import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ItemDataService } from '../services/item-data.service';
import { Item } from '../models/item';

@Component({
  selector: 'app-delete-item',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './delete-item.component.html',
  styleUrls: ['./delete-item.component.css']
})
export class DeleteItemComponent implements OnInit {
  item!: Item;                   // Holds the item to be deleted
  message: string = '';          // Holds status or error messages for the UI

  constructor(
    private router: Router,                  // Angular Router to navigate between views
    private itemDataService: ItemDataService // Service to interact with item data
  ) { }

  /**
   * Lifecycle hook that runs after component initialization.
   * Fetches the item code from localStorage and retrieves the item details.
   */
  ngOnInit(): void {
    try {
      // Retrieve the item code stored in localStorage
      const itemCode = localStorage.getItem("itemCode");
      if (!itemCode) {
        alert("Something went wrong, couldn't find the item code!");
        this.router.navigate(['']); // Redirect to item list
        return;
      }

      console.log('DeleteItemComponent::ngOnInit');
      console.log('itemCode:' + itemCode);

      // Retrieve the item details using the item code
      this.itemDataService.getItem(itemCode).subscribe({
        next: (value: any) => {
          if (value && value.length > 0) {
            this.item = value[0];
            this.message = 'Item: ' + itemCode + ' retrieved';
          } else {
            this.message = 'No Item Retrieved!';
            this.router.navigate(['']); // Redirect if item not found
          }
          console.log(this.message);
        },
        error: (error: any) => {
          // Log HTTP or service errors
          console.error('Error retrieving item:', error);
        }
      });
    } catch (error) {
      // Catch and log unexpected synchronous errors
      console.error('Unexpected error during ngOnInit:', error);
    }
  }

  /**
   * Cancels the deletion process and navigates back to the item list.
   */
  public cancelDelete(): void {
    try {
      this.router.navigate(['']);
    } catch (error) {
      // Catch navigation errors
      console.error('Error navigating back:', error);
    }
  }

  /**
   * Confirms and performs the delete operation.
   * If successful, navigates back to the item list.
   */
  public onDelete(): void {
    try {
      if (confirm('Are you sure you want to delete this item?')) {
        this.itemDataService.deleteItem(this.item.code).subscribe({
          next: () => {
            // Redirect to item list after successful deletion
            this.router.navigate(['']);
          },
          error: (error: any) => {
            // Log errors encountered during deletion
            console.error('Error deleting item:', error);
          }
        });
      }
    } catch (error) {
      // Catch unexpected synchronous errors
      console.error('Unexpected error during onDelete:', error);
    }
  }
}
