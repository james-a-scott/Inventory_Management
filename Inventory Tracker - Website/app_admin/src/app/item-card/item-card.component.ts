import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Item } from '../models/item';
import { AuthenticationService } from '../services/authentication.service';
import { ItemDataService } from '../services/item-data.service';

@Component({
  selector: 'app-item-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './item-card.component.html',
  styleUrl: './item-card.component.css',
})
export class ItemCardComponent implements OnInit {
  // Input property for passing an item to the component
  @Input('item') item: any;

  constructor(
    private router: Router,                    // Router to navigate to different pages
    private authenticationService: AuthenticationService, // Service for handling authentication
    private itemDataService: ItemDataService     // Service for interacting with item data
  ) { }

  ngOnInit(): void {
    // Lifecycle hook; currently no specific logic for initialization
  }

  /**
   * Checks if the user is logged in.
   * 
   * @returns {boolean} - Returns true if the user is logged in, otherwise false.
   * Catches and logs any error encountered while checking login status.
   */
  public isLoggedIn(): boolean {
    try {
      // Call authentication service to check login status
      return this.authenticationService.isLoggedIn();
    } catch (error) {
      // Log error if checking login status fails
      console.error('Error in isLoggedIn:', error);
      return false; // Default to false if an error occurs
    }
  }

  /**
   * Navigates to the edit item page and stores the item code in localStorage for reference.
   * 
   * @param {Item} item - The item object that needs to be edited.
   * Removes any previously stored item code and sets the new item code.
   */
  public editItem(item: Item): void {
    try {
      // Remove any previous item code and store the current one
      localStorage.removeItem('itemCode');
      localStorage.setItem('itemCode', item.code);

      // Navigate to the edit item page
      this.router.navigate(['edit-item']);
    } catch (error) {
      // Log any error that occurs during navigation or localStorage operations
      console.error('Error in editItem:', error);
    }
  }

  /**
   * Navigates to the delete item page and stores the item code in localStorage for reference.
   * 
   * @param {Item} item - The item object that needs to be deleted.
   * Removes any previously stored item code and sets the new item code.
   */
  public deleteItem(item: Item): void {
    try {
      // Remove any previous item code and store the current one
      localStorage.removeItem('itemCode');
      localStorage.setItem('itemCode', item.code);

      // Navigate to the delete item page
      this.router.navigate(['delete-item']);
    } catch (error) {
      // Log any error that occurs during navigation or localStorage operations
      console.error('Error in deleteItem:', error);
    }
  }
}
