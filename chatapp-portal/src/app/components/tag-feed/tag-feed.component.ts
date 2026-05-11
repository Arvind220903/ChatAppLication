import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PostService, Post } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { LucideAngularModule, Heart, MessageCircle, Send, Bookmark, MoreVertical, ArrowLeft } from 'lucide-angular';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tag-feed',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule, RouterModule],
  templateUrl: './tag-feed.component.html',
  styleUrl: './tag-feed.component.css'
})
export class TagFeedComponent implements OnInit {
  tagName: string = '';
  posts: Post[] = [];
  loading = false;

  // Icons
  HeartIcon = Heart;
  MessageCircleIcon = MessageCircle;
  SendIcon = Send;
  BookmarkIcon = Bookmark;
  MoreVerticalIcon = MoreVertical;
  ArrowLeftIcon = ArrowLeft;

  constructor(
    private route: ActivatedRoute,
    private postService: PostService,
    public userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.tagName = params['tag'];
      if (this.tagName) {
        this.loadPosts();
      }
    });
  }

  loadPosts() {
    this.loading = true;
    this.postService.getPostsByTag(this.tagName).subscribe({
      next: (data) => {
        this.posts = data || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.posts = [];
      }
    });
  }

  likePost(post: Post) {
    if (!post.postId) return;
    this.postService.likePost(post.postId).subscribe({
      next: (res) => {
        if (res === 'liked') {
          post.likedByUser = true;
          post.likeCount = (post.likeCount || 0) + 1;
        } else {
          post.likedByUser = false;
          post.likeCount = Math.max(0, (post.likeCount || 0) - 1);
        }
      }
    });
  }

  savePost(post: Post) {
    if (!post.postId) return;
    this.postService.savePost(post.postId).subscribe({
      next: (res) => {
        post.savedByUser = (res === 'saved');
      }
    });
  }

  addComment(post: Post) {
    if (!post.postId || !post.newCommentText?.trim() || !this.userService.currentUser) return;
    const userId = this.userService.currentUser.userId!;
    const text = post.newCommentText.trim();
    post.newCommentText = '';

    this.postService.addComment(post.postId, userId, text).subscribe({
      next: (comments) => {
        post.comments = comments;
      }
    });
  }

  goToProfile(username: string) {
    this.router.navigate(['/profile', username]);
  }
}
