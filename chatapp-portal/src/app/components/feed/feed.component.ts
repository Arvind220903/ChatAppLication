import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, filter, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PostService, Post } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { LucideAngularModule, Image, Video, BarChart2, Smile, MoreVertical, Heart, MessageCircle, Send, Bookmark, MapPin, Map } from 'lucide-angular';
import * as L from 'leaflet';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, LucideAngularModule],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  loading = true;
  error = '';
  toast = '';
  toastColor = '#10b981';

  newPostTitle = '';
  isPosting = false;

  tagInput = '';
  selectedTags: string[] = [];
  tagRecommendations: any[] = [];

  locationSearchQuery = '';
  locationRecommendations: any[] = [];
  selectedLocation: any = null;
  private locationSearchSubject = new Subject<string>();
  
  showMapPicker = false;
  private pickerMap: L.Map | undefined;
  private pickerMarker: L.Marker | undefined;

  // Icons
  ImageIcon = Image;
  VideoIcon = Video;
  BarChartIcon = BarChart2;
  SmileIcon = Smile;
  MoreVerticalIcon = MoreVertical;
  HeartIcon = Heart;
  MessageCircleIcon = MessageCircle;
  SendIcon = Send;
  BookmarkIcon = Bookmark;
  MapPinIcon = MapPin;
  MapIcon = Map;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    public userService: UserService,
    private router: Router,
    private http: HttpClient
  ) { }

  ngOnInit() {
    this.loadProfile();
    // Subscribe to URL changes to reload feed
    this.route.url.subscribe(() => {
      this.loadFeed();
    });

    // Debounce location search to prevent 429 Too Many Requests
    this.locationSearchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(query => {
      this.fetchLocationRecommendations(query);
    });

    // Automatically detect and set the user's location on load
    this.autoDetectLocation();
  }

  autoDetectLocation() {
    if (navigator.geolocation && !this.selectedLocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const lat = pos.coords.latitude;
          const lon = pos.coords.longitude;
          const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`;
          this.http.get<any>(url).subscribe({
            next: (data) => {
              if (data && data.display_name) {
                // Pre-fill the location badge
                this.selectedLocation = {
                  lat: lat.toString(),
                  lon: lon.toString(),
                  display_name: data.display_name
                };
              }
            },
            error: (err) => console.warn("Reverse geocoding failed", err)
          });
        },
        (err) => console.warn("Geolocation blocked", err),
        { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
      );
    }
  }

  loadFeed() {
    this.loading = true;
    let fetchObservable = this.postService.getFeed();

    if (this.router.url.includes('/saved')) {
      fetchObservable = this.postService.getSavedPosts();
    } else if (this.router.url.includes('/likes')) {
      fetchObservable = this.postService.getLikedPosts();
    } else if (this.router.url.includes('/comments')) {
      fetchObservable = this.postService.getCommentedPosts();
    } else if (this.router.url.includes('/explore')) {
      fetchObservable = this.postService.getTrending();
    }

    // Fetch posts + liked IDs + saved IDs all in parallel
    forkJoin({
      posts: fetchObservable.pipe(catchError(() => of([]))),
      likedPosts: this.postService.getLikedPosts().pipe(catchError(() => of([]))),
      savedPosts: this.postService.getSavedPosts().pipe(catchError(() => of([])))
    }).subscribe(({ posts, likedPosts, savedPosts }) => {
      const likedIds = new Set((likedPosts || []).map(p => p.postId));
      const savedIds = new Set((savedPosts || []).map(p => p.postId));

      this.posts = (posts || []).map(p => ({
        ...p,
        likedByUser: likedIds.has(p.postId),
        savedByUser: savedIds.has(p.postId),
        displayTime: this.calculateTimeAgo(p.createdAt)
      }));

      this.loading = false;
    });
  }

  loadProfile() {
    this.userService.getProfile().subscribe(); // Handled automatically by service subject
  }

  createPost() {
    if (!this.newPostTitle.trim() || !this.userService.currentUser) return;

    // Automatically convert pending tag input into a tag when posting
    const pendingTag = this.tagInput.trim();
    if (pendingTag) {
      if (!this.selectedTags.includes(pendingTag)) {
        this.selectedTags.push(pendingTag);
      }
      this.tagInput = '';
    }

    this.isPosting = true;

    if (this.selectedLocation) {
      this.submitPost(parseFloat(this.selectedLocation.lat), parseFloat(this.selectedLocation.lon));
    } else if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => this.submitPost(pos.coords.latitude, pos.coords.longitude),
        (err) => {
          console.warn("Geolocation blocked, posting without coords", err);
          this.submitPost(0, 0);
        },
        { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 }
      );
    } else {
      this.submitPost(0, 0);
    }
  }

  private submitPost(lat: number, lng: number) {
    const post: Post = {
      title: this.newPostTitle.trim(),
      user: this.userService.currentUser!.userId!,
      likeCount: 0,
      tags: this.selectedTags,
      latitude: lat,
      longitude: lng
    };

    this.postService.createPost(post).subscribe({
      next: () => {
        this.newPostTitle = '';
        this.selectedTags = [];
        this.tagInput = '';
        this.selectedLocation = null;
        this.locationSearchQuery = '';
        this.isPosting = false;
        this.loadFeed();
        // Re-detect location for the next post
        this.autoDetectLocation();
      },
      error: () => {
        alert('Failed to create post');
        this.isPosting = false;
      }
    });
  }

  likePost(post: Post) {
    if (!post.postId || !this.userService.currentUser) return;

    // Optimistic UI update
    post.likedByUser = !post.likedByUser;
    post.likeCount = post.likedByUser ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);

    this.postService.likePost(post.postId).subscribe({
      next: (res) => {
        post.likedByUser = res === 'liked';
        // Sync real like count from server
        this.postService.getLikes(post.postId!).subscribe({
          next: (count) => post.likeCount = count,
          error: () => { } // keep optimistic count on failure
        });
      },
      error: () => {
        // Revert optimistic update on failure
        post.likedByUser = !post.likedByUser;
        post.likeCount = post.likedByUser ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
        console.error('Like failed');
      }
    });
  }

  goToComments() {
    this.router.navigate(['/comments']);
  }

  goToProfile(username: string | undefined) {
    if (username) {
      this.router.navigate(['/profile', username]);
    }
  }

  savePost(post: Post) {
    if (!post.postId) return;
    this.postService.savePost(post.postId).subscribe({
      next: (res) => {
        if (res === 'saved') {
          post.savedByUser = true;
          this.showToast('\u2713 Saved!', '#10b981');
        } else {
          post.savedByUser = false;
          this.showToast('\u2715 Removed from saved', '#6b7280');
        }
      },
      error: () => this.showToast('Failed to save', '#ef4444')
    });
  }

  showToast(msg: string, color = '#10b981') {
    this.toast = msg;
    this.toastColor = color;
    setTimeout(() => this.toast = '', 2000);
  }

  addComment(post: Post) {
    if (!post.postId || !post.newCommentText?.trim() || !this.userService.currentUser) return;
    const userId = this.userService.currentUser.userId!;

    const text = post.newCommentText.trim();
    post.newCommentText = '';

    this.postService.addComment(post.postId, userId, text).subscribe({
      next: (commentsFromServer) => {
        post.comments = commentsFromServer;
      },
      error: () => {
        alert('Failed to add comment');
        post.newCommentText = text;
      }
    });
  }

  public calculateTimeAgo(date: string | undefined): string {
    if (!date) return '';
    const diff = Date.now() - new Date(date).getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'now';
    if (mins < 60) return `${mins}m`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h`;
    const days = Math.floor(hrs / 24);
    return `${days}d`;
  }

  onTagInput(event: any) {
    const val = this.tagInput;
    if (val.endsWith(' ') || val.endsWith('#')) {
      const tag = val.substring(0, val.length - 1).trim();
      if (tag && !this.selectedTags.includes(tag)) {
        this.selectedTags.push(tag);
      }
      this.tagInput = val.endsWith('#') ? '#' : '';
      this.tagRecommendations = [];
    } else if (val.trim().length > 1) {
      this.postService.findTags(val.trim()).subscribe({
        next: (res) => this.tagRecommendations = res,
        error: () => this.tagRecommendations = []
      });
    } else {
      this.tagRecommendations = [];
    }
  }

  addTagFromRecommendation(tag: string) {
    const cleanTag = tag.startsWith('#') ? tag.substring(1) : tag;
    if (!this.selectedTags.includes(cleanTag)) {
      this.selectedTags.push(cleanTag);
    }
    this.tagInput = '';
    this.tagRecommendations = [];
  }

  removeTag(index: number) {
    this.selectedTags.splice(index, 1);
  }

  onLocationSearchInput(): void {
    this.locationSearchSubject.next(this.locationSearchQuery);
  }

  private fetchLocationRecommendations(query: string): void {
    if (query.length < 3) {
      this.locationRecommendations = [];
      return;
    }

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=in&limit=5`;
    this.http.get<any[]>(url).subscribe({
      next: (data) => this.locationRecommendations = data,
      error: () => this.locationRecommendations = []
    });
  }

  selectLocation(rec: any): void {
    this.selectedLocation = rec;
    this.locationSearchQuery = '';
    this.locationRecommendations = [];
  }

  clearLocation(): void {
    this.selectedLocation = null;
    if (this.pickerMarker && this.pickerMap) {
      this.pickerMarker.setLatLng([19.0760, 72.8777]);
      this.pickerMap.setView([19.0760, 72.8777], 13);
    }
  }

  toggleMapPicker(): void {
    this.showMapPicker = !this.showMapPicker;
    if (this.showMapPicker) {
      setTimeout(() => this.initMapPicker(), 100);
    } else if (this.pickerMap) {
      this.pickerMap.remove();
      this.pickerMap = undefined;
    }
  }

  private initMapPicker(): void {
    let initialLat = 19.0760;
    let initialLng = 72.8777;

    if (this.selectedLocation) {
      initialLat = parseFloat(this.selectedLocation.lat);
      initialLng = parseFloat(this.selectedLocation.lon);
    }

    this.pickerMap = L.map('create-post-map', {
      center: [initialLat, initialLng],
      zoom: 13,
      zoomControl: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap &copy; CARTO'
    }).addTo(this.pickerMap);

    this.pickerMarker = L.marker([initialLat, initialLng], {
      draggable: true,
      icon: L.divIcon({
        className: 'custom-explorer-pin',
        html: '<div class="pin-inner"></div>',
        iconSize: [40, 40],
        iconAnchor: [20, 40]
      })
    }).addTo(this.pickerMap);

    this.pickerMarker.on('dragend', (e: any) => {
      const position = e.target.getLatLng();
      this.reverseGeocode(position.lat, position.lng);
    });
  }

  private reverseGeocode(lat: number, lon: number): void {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lon}`;
    this.http.get<any>(url).subscribe({
      next: (data) => {
        if (data && data.display_name) {
          this.selectedLocation = {
            lat: lat.toString(),
            lon: lon.toString(),
            display_name: data.display_name
          };
        }
      },
      error: (err) => console.warn("Reverse geocode failed", err)
    });
  }
}
