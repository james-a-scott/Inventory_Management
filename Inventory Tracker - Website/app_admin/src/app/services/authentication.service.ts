import { Inject, Injectable } from '@angular/core';
import { BROWSER_STORAGE } from '../storage';
import { User } from '../models/user';
import { AuthResponse } from '../models/authresponse';
import { ItemDataService } from '../services/item-data.service';

@Injectable({
  providedIn: 'root',
})
export class AuthenticationService {

  // Setup our storage and service access
  constructor(
    @Inject(BROWSER_STORAGE) private storage: Storage,
    private ItemDataService: ItemDataService
  ) { }

  // Variable to handle Authentication Responses
  authResp: AuthResponse = new AuthResponse();

  // Get our token from our Storage provider.
  // NOTE: For this application we have decided that we will name
  // the key for our token 'inventory-token'
  public getToken(): string {
    let out: any;
    out = this.storage.getItem('inventory-token');
    // Make sure we return a string even if we don't have a token
    if (!out) {
      return '';
    }
    return out;
  }
  // Save our token to our Storage provider.
  // NOTE: For this application we have decided that we will name
  // the key for our token 'inventory-token'
  public saveToken(token: string): void {
    this.storage.setItem('inventory-token', token);
  }
  // Logout of our application and remove the JWT from Storage
  public logout(): void {
    this.storage.removeItem('inventory-token');
  }


  // Boolean to determine if we are logged in and the token is
  // still valid. Even if we have a token we will still have to
  // reauthenticate if the token has expired
  public isLoggedIn(): boolean {
    const token: string = this.getToken();
    if (token) {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp > (Date.now() / 1000);
    } else {
      return false;
    }
  }
  // Retrieve the current user. This function should only be called
  // after the calling method has checked to make sure that the user
  // isLoggedIn.
  public getCurrentUser(): User {
    const token: string = this.getToken();
    const payload = JSON.parse(atob(token.split('.')[1]));
    return { email: payload.email, role: payload.role } as User;
  }

  // Login method that leverages the login method in ItemDataService
  // Because that method returns an observable, we subscribe to the
  // result and only process when the Observable condition is satisfied
  // Uncomment the two console.log messages for additional debugging
  // information.
  public login(user: User, passwd: string): void {
    this.ItemDataService.login(user, passwd).subscribe({
      next: (value: any) => {
        if (value) {
          console.log(value);
          this.authResp = value;
          this.saveToken(this.authResp.token);
        }
      },
      error: (error: any) => {
        console.log('Error: ' + error);
      }
    })
  }
  // Register method that leverages the register method in
  // ItemDataService
  // Because that method returns an observable, we subscribe to the
  // result and only process when the Observable condition is satisfied
  // Uncomment the two console.log messages for additional debugging
  // information. Please Note: This method is nearly identical to the
  // login method because the behavior of the API logs a new user in
  // immediately upon registration
  public register(user: User, passwd: string): void {
    this.ItemDataService.register(user, passwd).subscribe({
      next: (value: any) => {
        console.log('Registration response:', value);

      },
      error: (error: any) => {
        console.log('Error during registration:', error);
      }
    });
  }



  // Check if the user is an Admin
  public isAdmin(): boolean {
    return this.isLoggedIn() && this.getCurrentUser().role === 'Admin';
  }

  // Check if the user is a SuperUser
  public isSuperUser(): boolean {
    return this.isLoggedIn() && this.getCurrentUser().role === 'SuperUser';
  }

  // Check if the user is a regular User
  public isRegularUser(): boolean {
    return this.isLoggedIn() && this.getCurrentUser().role === 'User';
  }

  // Check if the user can edit (Admin or SuperUser)
  public canEdit(): boolean {
    return this.isAdmin() || this.isSuperUser();
  }

  // Check if the user can delete (Admin only)
  public canDelete(): boolean {
    return this.isAdmin();
  }

}
