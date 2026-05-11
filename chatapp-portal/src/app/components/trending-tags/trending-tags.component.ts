import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService, Post } from '../../services/post.service';
import { LucideAngularModule, Hash, MessageCircle, Heart, Send, Bookmark, MoreVertical, X, Search } from 'lucide-angular';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-trending-tags',
  standalone: true,
  imports: [CommonModule, LucideAngularModule, FormsModule],
  templateUrl: './trending-tags.component.html',
  styleUrl: './trending-tags.component.css'
})
export class TrendingTagsComponent implements OnInit {
  tags: any[] = [];
  searchResults: string[] = [];
  tagPosts: Post[] = [];
  loading = false;

  HashIcon = Hash;
  MessageCircleIcon = MessageCircle;
  HeartIcon = Heart;
  SendIcon = Send;
  BookmarkIcon = Bookmark;
  MoreVerticalIcon = MoreVertical;
  SearchIcon = Search;
  XIcon = X;

  searchQuery = '';
  private searchSubject = new Subject<string>();
  
  constructor(private postService: PostService, private router: Router) {
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(query => {
      this.executeSearch(query);
    });
  }

  ngOnInit() {
    this.loadTags();
  }

  loadTags() {
    this.loading = true;
    this.postService.getTrendingTags().subscribe({
      next: (data) => {
        this.tags = data || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  goToTag(tagName: string) {
    this.searchQuery = tagName;
    this.executeSearch(tagName);
  }

  onSearchInput() {
    this.searchSubject.next(this.searchQuery);
  }

  executeSearch(query: string) {
    if (!query.trim()) {
      this.searchResults = [];
      return;
    }
    this.postService.findTags(query.trim()).subscribe({
      next: (data: any) => {
        this.searchResults = data || [];
      },
      error: () => {
        this.searchResults = [];
      }
    });
  }

  selectTag(tagName: string) {
    // Remove # if present for the navigation
    const cleanTag = tagName.startsWith('#') ? tagName.substring(1) : tagName;
    this.router.navigate(['/tag', cleanTag]);
  }

  clearSearch() {
    this.searchQuery = '';
    this.searchResults = [];
  }
}
