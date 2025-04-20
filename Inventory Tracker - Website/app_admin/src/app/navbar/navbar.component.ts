import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit {
  // Inject AuthenticationService to manage login/logout status
  constructor(
    private authenticationService: AuthenticationService
  ) { }

  // Lifecycle hook to perform initialization logic when the component is created
  ngOnInit(): void { }

  /**
   * Checks if the user is logged in by calling the authentication service.
   * This method returns a boolean indicating the login status.
   * 
   * @returns boolean - Returns true if the user is logged in, otherwise false.
   * @throws Error - Logs and returns false if an error occurs while checking login status.
   */
  public isLoggedIn(): boolean {
    try {
      // Attempt to check if the user is logged in via AuthenticationService
      return this.authenticationService.isLoggedIn();
    } catch (error) {
      // Log any error that occurs while checking login status
      console.error('Error checking login status:', error);
      return false; // Return false if there's an error
    }
  }

  /**
   * Handles user logout by calling the logout method in AuthenticationService.
   * Ensures that any errors during the logout process are caught and logged.
   * 
   * @throws Error - Logs and handles any issues encountered during the logout process.
   */
  public onLogout(): void {
    try {
      // Attempt to log out the user via AuthenticationService
      this.authenticationService.logout();
    } catch (error) {
      // Log any error encountered during the logout process
      console.error('Error during logout:', error);
    }
  }
}
