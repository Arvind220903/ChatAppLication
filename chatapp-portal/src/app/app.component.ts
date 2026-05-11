import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';
import { UserService, User } from './services/user.service';
import { PostService } from './services/post.service';
import { LucideAngularModule, Search, Bell, Mail, Home, Compass, Bookmark, Save, Heart, Settings, LogOut, PlusSquare, X, Users, Hash, Map } from 'lucide-angular';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterModule, FormsModule, LucideAngularModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  searchQuery = '';
  searchResults: string[] = [];
  showDropdown = false;
  private searchSubject = new Subject<string>();

  // Icons used in template
  SearchIcon = Search;
  BellIcon = Bell;
  MailIcon = Mail;
  HomeIcon = Home;
  CompassIcon = Compass;
  BookmarkIcon = Bookmark;
  SaveIcon = Save;
  HeartIcon = Heart;
  SettingsIcon = Settings;
  LogOutIcon = LogOut;
  PlusSquareIcon = PlusSquare;
  XIcon = X;
  UsersIcon = Users;
  HashIcon = Hash;
  MapIcon = Map;

  // Notifications
  notifications: any[] = [];
  showNotifications = false;
  notifLoading = false;
  unreadNotificationsCount = 0;

  // Followers / Following
  followers: User[] = [];
  followings: User[] = [];
  showFollowers = false;
  showFollowings = false;
  listLoading = false;

  // Trending Tags
  trendingTags: any[] = [];
  tagsLoading = false;

  constructor(public userService: UserService, public router: Router, private postService: PostService) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      this.executeSearch(query);
    });

    // Auto-load notifications whenever user logs in
    this.userService.currentUser$.pipe(
      filter(user => user !== null)
    ).subscribe(() => {
      this.loadNotifications();
      this.loadTrendingTags();
    });
  }

  get isLoggedIn(): boolean {
    return this.userService.token !== null;
  }

  toggleNotifications() {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      // 1. Instant UI update: Reset count locally right away
      const current = this.userService.currentUser;
      if (current) {
        this.userService['currentUserSubject'].next({ ...current, unseenNoti: 0 });
      }

      // 2. Notify backend to clear it in the DB
      this.userService.clearNotifications().subscribe();
      this.loadNotifications();
    }
  }

  loadNotifications() {
    this.notifLoading = true;
    this.userService.getNotifications().subscribe({
      next: (data) => {
        this.notifications = data || [];
        if (!this.showNotifications) {
          this.unreadNotificationsCount = this.notifications.length;
        }
        this.notifLoading = false;
      },
      error: () => {
        this.notifications = [];
        this.notifLoading = false;
      }
    });
  }

  logout() {
    this.userService.logout();
    this.router.navigate(['/login']);
  }

  onSearchInput() {
    this.searchSubject.next(this.searchQuery);
  }

  executeSearch(query: string) {
    if (!query.trim()) {
      this.searchResults = [];
      this.showDropdown = false;
      return;
    }

    this.userService.searchUser(query.trim()).subscribe({
      next: (results) => {
        this.searchResults = results || [];
        this.showDropdown = true;
      },
      error: () => {
        this.searchResults = [];
        this.showDropdown = false;
      }
    });
  }

  selectSearchResult(result: string) {
    this.searchQuery = '';
    this.showDropdown = false;
    this.router.navigate(['/profile', result]);
  }

  toggleFollowers() {
    this.showFollowers = !this.showFollowers;
    if (this.showFollowers) {
      this.showFollowings = false;
      this.loadFollowers();
    }
  }

  toggleFollowing() {
    this.showFollowings = !this.showFollowings;
    if (this.showFollowings) {
      this.showFollowers = false;
      this.loadFollowing();
    }
  }

  loadFollowers() {
    const user = this.userService.currentUser;
    if (!user) return;
    this.listLoading = true;
    this.userService.getFollowers(user.userEmail).subscribe({
      next: (data) => { this.followers = data || []; this.listLoading = false; },
      error: () => { this.followers = []; this.listLoading = false; }
    });
  }

  loadFollowing() {
    const user = this.userService.currentUser;
    if (!user) return;
    this.listLoading = true;
    this.userService.getFollowing(user.userEmail).subscribe({
      next: (data) => { this.followings = data || []; this.listLoading = false; },
      error: () => { this.followings = []; this.listLoading = false; }
    });
  }

  navigateToUser(username: string) {
    this.showFollowers = false;
    this.showFollowings = false;
    this.router.navigate(['/profile', username]);
  }

  loadTrendingTags() {
    this.tagsLoading = true;
    this.postService.getTrendingTags().subscribe({
      next: (data) => {
        this.trendingTags = (data || []).slice(0, 5); // Take top 5
        this.tagsLoading = false;
      },
      error: () => {
        this.trendingTags = [];
        this.tagsLoading = false;
      }
    });
  }
}
