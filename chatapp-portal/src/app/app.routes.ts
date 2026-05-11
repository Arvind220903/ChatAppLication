import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./components/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./components/register/register.component').then(m => m.RegisterComponent) },
  { path: 'feed', loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent) },
  { path: 'saved', loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent) },
  { path: 'likes', loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent) },
  { path: 'comments', loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent) },
  { path: 'explore', loadComponent: () => import('./components/feed/feed.component').then(m => m.FeedComponent) },
  { path: 'map', loadComponent: () => import('./components/explorer/explorer.component').then(m => m.ExplorerComponent) },
  { path: 'trending-tags', loadComponent: () => import('./components/trending-tags/trending-tags.component').then(m => m.TrendingTagsComponent) },
  { path: 'tag/:tag', loadComponent: () => import('./components/tag-feed/tag-feed.component').then(m => m.TagFeedComponent) },
  { path: 'messages', loadComponent: () => import('./components/chat/chat.component').then(m => m.ChatComponent) },
  { path: 'profile/:username', loadComponent: () => import('./components/profile/profile.component').then(m => m.ProfileComponent) }
];
