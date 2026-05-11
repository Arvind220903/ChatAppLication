import { Component, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import * as L from 'leaflet';
import { LucideAngularModule, Search, MapPin, Navigation, LocateFixed } from 'lucide-angular';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { PostService } from '../../services/post.service';

@Component({
  selector: 'app-explorer',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './explorer.component.html',
  styleUrls: ['./explorer.component.css']
})
export class ExplorerComponent implements AfterViewInit, OnDestroy {
  private map!: L.Map;
  private explorerPin!: L.Marker;
  private markers: any[] = [];
  
  lat: number = 19.0760;
  lng: number = 72.8777;
  searchQuery: string = '';
  posts: any[] = [];
  loading: boolean = false;
  recommendations: any[] = [];
  private searchSubject = new Subject<string>();

  readonly SearchIcon = Search;
  readonly MapPinIcon = MapPin;
  readonly NavigationIcon = Navigation;
  readonly LocateIcon = LocateFixed;

  constructor(private http: HttpClient, private postService: PostService) {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged()
    ).subscribe(query => {
      this.fetchRecommendations(query);
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
    }
  }

  private initMap(): void {
    this.map = L.map('map-container', {
      center: [this.lat, this.lng],
      zoom: 13,
      zoomControl: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap contributors &copy; CARTO'
    }).addTo(this.map);

    L.control.zoom({ position: 'bottomright' }).addTo(this.map);

    // Initial Marker
    this.explorerPin = L.marker([this.lat, this.lng], {
      draggable: true,
      icon: L.divIcon({
        className: 'custom-explorer-pin',
        html: '<div class="pin-inner"></div>',
        iconSize: [40, 40],
        iconAnchor: [20, 40]
      })
    }).addTo(this.map);

    this.explorerPin.on('dragend', (e: any) => {
      const position = e.target.getLatLng();
      this.lat = position.lat;
      this.lng = position.lng;
      this.fetchPostsByRegion();
    });

    // Use setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
    setTimeout(() => this.fetchPostsByRegion());
  }

  fetchPostsByRegion(): void {
    this.loading = true;
    // Clear old markers
    this.markers.forEach(m => this.map.removeLayer(m));
    this.markers = [];

    this.postService.getRegionPosts(this.lat, this.lng)
      .subscribe({
        next: (data) => {
          this.posts = data;
          this.posts.forEach(post => {
            const m = L.circleMarker([post.latitude, post.longitude], {
              radius: 8,
              fillColor: "#e11d48",
              color: "#fff",
              weight: 2,
              opacity: 1,
              fillOpacity: 0.8
            }).addTo(this.map);
            
            m.bindPopup(`<b>@${post.userName || 'Anonymous'}</b><br>${post.title}`);
            this.markers.push(m);
          });
          this.loading = false;
        },
        error: (err) => {
          console.error("Failed to fetch posts", err);
          this.loading = false;
        }
      });
  }

  searchCity(): void {
    if (!this.searchQuery) return;

    // Added countrycodes=in to restrict search to India for better accuracy
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.searchQuery)}&countrycodes=in&limit=1`;
    
    this.http.get<any[]>(url)
      .subscribe({
        next: (data) => {
          if (data.length > 0) {
            this.lat = parseFloat(data[0].lat);
            this.lng = parseFloat(data[0].lon);
            
            this.map.setView([this.lat, this.lng], 13);
            this.explorerPin.setLatLng([this.lat, this.lng]);
            this.fetchPostsByRegion();
          } else {
            alert("Location not found in India. Please be more specific (e.g. 'Lonavala, Maharashtra')");
          }
        },
        error: (err) => {
          console.error("Search failed", err);
        }
      });
  }

  locateMe(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        this.lat = pos.coords.latitude;
        this.lng = pos.coords.longitude;
        this.map.setView([this.lat, this.lng], 13);
        this.explorerPin.setLatLng([this.lat, this.lng]);
        this.fetchPostsByRegion();
      }, (err) => {
        alert("Could not get your location. Please check browser permissions.");
      }, { enableHighAccuracy: true, timeout: 5000, maximumAge: 0 });
    }
  }

  onSearchInput(): void {
    this.searchSubject.next(this.searchQuery);
  }

  private fetchRecommendations(query: string): void {
    if (query.length < 3) {
      this.recommendations = [];
      return;
    }

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&countrycodes=in&limit=5`;
    this.http.get<any[]>(url).subscribe(data => {
      this.recommendations = data;
    });
  }

  selectRecommendation(rec: any): void {
    this.lat = parseFloat(rec.lat);
    this.lng = parseFloat(rec.lon);
    this.searchQuery = rec.display_name;
    this.recommendations = [];
    
    this.map.setView([this.lat, this.lng], 13);
    this.explorerPin.setLatLng([this.lat, this.lng]);
    this.fetchPostsByRegion();
  }
}
