import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserService, User } from '../../services/user.service';
import { PostService, Post } from '../../services/post.service';
import {
  LucideAngularModule,
  Heart, MessageCircle, Send, Bookmark, MoreVertical, X
} from 'lucide-angular';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  profileUser: User | null = null;
  posts: Post[] = [];
  commentedPosts: Post[] = [];
  activeTab: 'posts' | 'comments' = 'posts';
  loading = true;
  isFollowing = false;
  isOwnProfile = false;

  HeartIcon = Heart;
  MessageCircleIcon = MessageCircle;
  SendIcon = Send;
  BookmarkIcon = Bookmark;
  MoreVerticalIcon = MoreVertical;
  XIcon = X;

  // Followers / Following
  followers: User[] = [];
  followings: User[] = [];
  showFollowers = false;
  showFollowings = false;
  listLoading = false;

  // Edit Profile
  showEditModal = false;
  editBio = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public userService: UserService,
    private postService: PostService
  ) {}

  openEditModal() {
    this.editBio = this.profileUser?.userbio || '';
    this.showEditModal = true;
  }

  goToChat() {
    if (this.profileUser?.userId) {
      this.router.navigate(['/messages'], { 
        queryParams: { 
          userId: this.profileUser.userId,
          userName: this.profileUser.userName 
        } 
      });
    }
  }

  saveProfile() {
    if (!this.profileUser) return;
    const updatedUser = { ...this.profileUser, userbio: this.editBio };
    this.userService.updateProfile(updatedUser).subscribe({
      next: (user) => {
        this.profileUser = user;
        this.showEditModal = false;
      },
      error: () => alert('Failed to update profile')
    });
  }

  deletePost(post: Post) {
    if (!post.postId || !this.userService.currentUser?.userId) return;
    
    // Inline confirmation check
    if (!(post as any).confirmDelete) {
      (post as any).confirmDelete = true;
      setTimeout(() => (post as any).confirmDelete = false, 3000); // reset after 3s
      return;
    }

    this.postService.deletePost(post.postId, this.userService.currentUser.userId).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.postId !== post.postId);
        this.commentedPosts = this.commentedPosts.filter(p => p.postId !== post.postId);
      },
      error: () => alert('Failed to delete post')
    });
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
    if (!this.profileUser?.userEmail) {
      console.warn('Cannot load followers: profileUser email is missing');
      return;
    }
    this.listLoading = true;
    this.userService.getFollowers(this.profileUser.userEmail).subscribe({
      next: (data) => { 
        this.followers = data || []; 
        this.listLoading = false; 
        console.log(`Loaded ${this.followers.length} followers for ${this.profileUser?.userEmail}`);
      },
      error: (err) => { 
        console.error('Failed to load followers', err);
        this.followers = []; 
        this.listLoading = false; 
      }
    });
  }

  loadFollowing() {
    if (!this.profileUser?.userEmail) {
      console.warn('Cannot load following: profileUser email is missing');
      return;
    }
    this.listLoading = true;
    this.userService.getFollowing(this.profileUser.userEmail).subscribe({
      next: (data) => { 
        this.followings = data || []; 
        this.listLoading = false; 
        console.log(`Loaded ${this.followings.length} following for ${this.profileUser?.userEmail}`);
      },
      error: (err) => { 
        console.error('Failed to load following', err);
        this.followings = []; 
        this.listLoading = false; 
      }
    });
  }

  navigateToUser(username: string) {
    this.showFollowers = false;
    this.showFollowings = false;
    this.router.navigate(['/profile', username]);
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      const username = params['username'];
      if (username) this.loadProfile(username);
    });
  }

  loadProfile(username: string) {
    this.loading = true;
    this.posts = [];
    this.followers = [];
    this.followings = [];
    this.profileUser = null;
    this.showFollowers = false;
    this.showFollowings = false;

    this.userService.getProfileByUsername(username).subscribe({
      next: (user) => {
        this.profileUser = user;
        const me = this.userService.currentUser;
        this.isOwnProfile = me?.userName === user.userName;
        this.isFollowing = !!(me?.following?.includes(user.userId!));

        if (user.userId) {
          // Load user's own posts
          this.postService.getPostsByUser(user.userId).subscribe({
            next: (p) => { this.posts = p || []; this.loading = false; },
            error: () => { this.posts = []; this.loading = false; }
          });

          // Load user's commented posts
          this.userService.getCommentedPostsByUser(user.userEmail).subscribe({
            next: (cp) => { this.commentedPosts = cp || []; },
            error: () => { this.commentedPosts = []; }
          });
        } else {
          this.loading = false;
        }
      },
      error: () => { this.loading = false; }
    });
  }

  toggleFollow() {
    if (!this.profileUser?.userId || !this.userService.currentUser) return;

    const targetUserId = this.profileUser.userId;
    const me = this.userService.currentUser;
    
    // 1. Store original state for rollback
    const wasFollowing = this.isFollowing;
    const originalFollowers = [...(this.profileUser.follower || [])];
    const originalFollowerCount = this.profileUser.followerCount || 0;

    // 2. Optimistic Update (Immediate UI Change)
    this.isFollowing = !this.isFollowing;
    
    if (this.isFollowing) {
      this.profileUser.followerCount = originalFollowerCount + 1;
      this.profileUser.follower = [...originalFollowers, me.userId!];
      me.following = [...(me.following || []), targetUserId];
      me.followingCount = (me.followingCount || 0) + 1;
    } else {
      this.profileUser.followerCount = Math.max(0, originalFollowerCount - 1);
      this.profileUser.follower = originalFollowers.filter(id => id !== me.userId);
      me.following = (me.following || []).filter(id => id !== targetUserId);
      me.followingCount = Math.max(0, (me.followingCount || 1) - 1);
    }

    // 3. Broadcast changes to sidebar
    this.userService.updateLocalUser(me);

    // 4. Backend Call
    this.userService.followUser(targetUserId).subscribe({
      next: (res) => {
        console.log('Follow status updated:', res);
      },
      error: (err) => {
        // 4. Rollback on failure
        console.error('Follow failed, reverting state', err);
        this.isFollowing = wasFollowing;
        this.profileUser!.follower = originalFollowers;
        this.profileUser!.followerCount = originalFollowerCount;
        
        if (wasFollowing) {
          me.following = [...(me.following || []), targetUserId];
        } else {
          me.following = (me.following || []).filter(id => id !== targetUserId);
        }
        alert('Action failed. Please try again.');
      }
    });
  }

  goToProfile(username: string | undefined) {
    if (username) this.router.navigate(['/profile', username]);
  }

  likePost(post: Post) {
    if (!post.postId || !this.userService.currentUser) return;
    post.likedByUser = !post.likedByUser;
    post.likeCount = post.likedByUser ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);

    this.postService.likePost(post.postId).subscribe({
      next: (res) => {
        post.likedByUser = res === 'liked';
        this.postService.getLikes(post.postId!).subscribe(count => post.likeCount = count);
      },
      error: () => {
        post.likedByUser = !post.likedByUser;
        post.likeCount = post.likedByUser ? (post.likeCount || 0) + 1 : Math.max(0, (post.likeCount || 1) - 1);
      }
    });
  }

  savePost(post: Post) {
    if (!post.postId) return;
    this.postService.savePost(post.postId).subscribe({
      next: (res) => { post.savedByUser = res === 'saved'; },
      error: () => {}
    });
  }

  addComment(post: Post) {
    if (!post.postId || !post.newCommentText?.trim() || !this.userService.currentUser) return;
    const userId = this.userService.currentUser.userId!;
    const text = post.newCommentText.trim();
    post.newCommentText = '';

    this.postService.addComment(post.postId, userId, text).subscribe({
      next: (comments) => { post.comments = comments; },
      error: () => { post.newCommentText = text; alert('Failed to add comment'); }
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
    return `${Math.floor(hrs / 24)}d`;
  }
}
