import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';
  loading = false;

  constructor(private userService: UserService, private router: Router) {
    if (this.userService.token) {
      this.router.navigate(['/feed']);
    }
  }

  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true;
    this.error = '';

    this.userService.login(this.email, this.password).subscribe({
      next: (token) => {
        // Token is saved inside service, now fetch profile
        this.userService.getProfile().subscribe({
          next: () => {
            this.router.navigate(['/feed']);
          },
          error: () => {
            this.error = 'Failed to load profile. Please try again.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = err.error || 'Invalid credentials. Please try again.';
        this.loading = false;
      }
    });
  }
}
