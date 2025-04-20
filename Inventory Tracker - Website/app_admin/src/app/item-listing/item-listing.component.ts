import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ItemCardComponent } from '../item-card/item-card.component';
import { ItemDataService } from '../services/item-data.service';
import { Item } from '../models/item';
import { AuthenticationService } from '../services/authentication.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-item-listing',
  standalone: true,
  imports: [CommonModule, ItemCardComponent, FormsModule],
  templateUrl: './item-listing.component.html',
  styleUrls: ['./item-listing.component.css'],
  providers: [ItemDataService],
})
export class ItemListingComponent implements OnInit {
  items: Item[] = [];
  filteredItems: Item[] = [];
  searchTerm: string = '';
  message: string = '';
  userRole: string = '';

  currentPage: number = 1;
  pageSize: number = 10;
  isLoading: boolean = true;

  // Sorting variables
  sortBy: string = 'name'; // Default sorting by name
  sortOrder: string = 'asc'; // Default sorting order (ascending)

  constructor(
    private itemDataService: ItemDataService,
    private router: Router,
    public authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    this.getStuff(); // Fetch the initial list of items when the component is initialized
  }

  // Industry Standard Comment: Check if the user is logged in.
  public isLoggedIn(): boolean {
    return this.authenticationService.isLoggedIn();
  }

  // Industry Standard Comment: Check if the logged-in user has admin privileges.
  public isAdmin(): boolean {
    return this.authenticationService.isAdmin();
  }

  // Industry Standard Comment: Check if the logged-in user has permission to edit items.
  public canEdit(): boolean {
    return this.authenticationService.canEdit();
  }

  // Industry Standard Comment: Check if the logged-in user has permission to delete items.
  public canDelete(): boolean {
    return this.authenticationService.canDelete();
  }

  // Industry Standard Comment: Navigate to the 'add-item' page to create a new item.
  public addItem(): void {
    this.router.navigate(['add-item']);
  }

  // Industry Standard Comment: Navigate to the 'register' page for user registration.
  public addUser(): void {
    this.router.navigate(['register']);
  }

  /**
   * Fetch the list of items from the server via the ItemDataService.
   * On success, update the list of items and reset pagination.
   * On failure, log the error and show an appropriate message to the user.
   */
  private getStuff(): void {
    try {
      this.itemDataService.getItems().subscribe({
        next: (value: any) => {
          // Successful data retrieval: set the items and reset pagination
          this.items = value;
          this.filteredItems = value;
          this.currentPage = 1;
          this.isLoading = false;
          this.updateMessage();
          this.sortItems(); // Sort items after fetching
        },
        error: (error: any) => {
          // Handle error: log the error and display a user-friendly message
          console.error('Error fetching items:', error);
          this.isLoading = false;
          this.message = 'Error fetching items. Please try again later.';
        },
      });
    } catch (error) {
      // Catch unexpected errors during execution and provide feedback
      console.error('Error in getStuff:', error);
      this.isLoading = false;
      this.message = 'An unexpected error occurred. Please try again later.';
    }
  }

  /**
   * Update the message displayed based on the number of filtered items.
   * If items are available, show the count, else show a no items message.
   */
  private updateMessage(): void {
    try {
      if (this.filteredItems.length > 0) {
        this.message = 'There are ' + this.filteredItems.length + ' items available.';
      } else {
        this.message = 'There were no items retrieved from the database.';
      }
    } catch (error) {
      // Log error if message update fails
      console.error('Error updating message:', error);
    }
  }

  /**
   * Handle search functionality by filtering items based on the search term.
   * Re-sort the list after the search operation.
   */
  public onSearch(): void {
    try {
      const term = this.searchTerm.toLowerCase().trim();

      if (term === '') {
        this.filteredItems = this.items;
      } else {
        this.filteredItems = this.items.filter(item =>
          item.name.toLowerCase().includes(term) ||
          (item.code && item.code.toLowerCase().includes(term))
        );
      }
      this.currentPage = 1;
      this.updateMessage();
      this.sortItems(); // Re-sort after search
    } catch (error) {
      // Log error if search fails
      console.error('Error in onSearch:', error);
    }
  }

  /**
   * Sort the items based on the selected criteria and order (name or quantity).
   * Default sorting is by name in ascending order.
   */
  public sortItems(): void {
    try {
      if (this.sortBy === 'name') {
        this.filteredItems.sort((a, b) => {
          return this.sortOrder === 'asc'
            ? a.name.localeCompare(b.name)
            : b.name.localeCompare(a.name);
        });
      } else if (this.sortBy === 'quantity') {
        this.filteredItems.sort((a, b) => {
          return this.sortOrder === 'asc'
            ? a.quantity - b.quantity
            : b.quantity - a.quantity;
        });
      }
    } catch (error) {
      // Log error if sorting fails
      console.error('Error in sortItems:', error);
    }
  }

  /**
   * Paginate the items by slicing the filtered items array based on current page and page size.
   * Returns a subset of items to be displayed on the current page.
   */
  public paginatedItems(): Item[] {
    try {
      if (!this.filteredItems) {
        console.error('filteredItems is undefined');
        return [];
      }
      const start = (this.currentPage - 1) * this.pageSize;
      return this.filteredItems.slice(start, start + this.pageSize);
    } catch (error) {
      // Log error if pagination fails
      console.error('Error in paginatedItems:', error);
      return [];
    }
  }

  /**
   * Calculate the total number of pages based on the filtered items and page size.
   * This is used to determine how many page buttons need to be shown.
   */
  public totalPages(): number {
    try {
      return Math.ceil(this.filteredItems.length / this.pageSize);
    } catch (error) {
      // Log error if total pages calculation fails
      console.error('Error calculating totalPages:', error);
      return 0;
    }
  }

  /**
   * Generate an array of page numbers for pagination.
   * This will be used to create page number buttons in the UI.
   */
  public totalPagesArray(): number[] {
    try {
      return Array.from({ length: this.totalPages() }, (_, i) => i + 1);
    } catch (error) {
      // Log error if total pages array generation fails
      console.error('Error in totalPagesArray:', error);
      return [];
    }
  }

  /**
   * Change the current page to the specified page number, if valid.
   * This allows the user to navigate between pages.
   */
  public changePage(page: number): void {
    try {
      if (page >= 1 && page <= this.totalPages()) {
        this.currentPage = page;
      }
    } catch (error) {
      // Log error if page change fails
      console.error('Error in changePage:', error);
    }
  }

  /**
   * Decrease the quantity of the specified item and update it in the database.
   * The quantity will not go below zero.
   */
  public decreaseQuantity(item: Item): void {
    try {
      if (item.quantity > 0) {
        item.quantity--;
        this.updateItemQuantity(item);
      }
    } catch (error) {
      // Log error if decreasing quantity fails
      console.error('Error decreasing quantity for item:', item, error);
    }
  }

  /**
   * Increase the quantity of the specified item and update it in the database.
   */
  public increaseQuantity(item: Item): void {
    try {
      item.quantity++;
      this.updateItemQuantity(item);
    } catch (error) {
      // Log error if increasing quantity fails
      console.error('Error increasing quantity for item:', item, error);
    }
  }

  /**
   * Update the quantity of the specified item in the database.
   * This involves calling the ItemDataService to persist the change.
   */
  private updateItemQuantity(item: Item): void {
    try {
      this.itemDataService.updateItem(item).subscribe({
        next: () => {
          console.log(`Quantity updated for ${item.name}`);
        },
        error: (error: any) => {
          // Log error if updating quantity fails
          console.error('Failed to update quantity:', error);
        }
      });
    } catch (error) {
      // Log error if the update fails
      console.error('Error in updateItemQuantity:', error);
    }
  }

  /**
   * Navigate to the item editing page with the specified item's code.
   */
  public editItem(item: Item): void {
    try {
      this.router.navigate(['edit-item', item.code]);
    } catch (error) {
      // Log error if navigation fails
      console.error('Error in editItem:', error);
    }
  }

  /**
   * Delete the specified item after confirming with the user.
   * If confirmed, the item will be removed from the database.
   */
  public deleteItem(item: Item): void {
    try {
      const confirmDelete = confirm(`Are you sure you want to delete "${item.name}"?`);
      if (confirmDelete) {
        this.itemDataService.deleteItem(item.code).subscribe({
          next: () => {
            this.getStuff(); // Refresh the item list after deletion
          },
          error: (error: any) => {
            // Log error if deletion fails
            console.error('Error deleting item:', error);
          },
        });
      }
    } catch (error) {
      // Log error if delete operation fails
      console.error('Error in deleteItem:', error);
    }
  }

  // Sorting helpers for template use
  /**
   * Change the sorting criteria based on the selected column.
   * This triggers the re-sorting of the item list.
   */
  public changeSortBy(sortBy: string): void {
    try {
      this.sortBy = sortBy;
      this.sortItems(); // Re-sort items whenever the sorting criteria changes
    } catch (error) {
      // Log error if changing sort criteria fails
      console.error('Error in changeSortBy:', error);
    }
  }

  /**
   * Toggle the sorting order between ascending and descending.
   * This allows the user to change the direction of sorting.
   */
  public toggleSortOrder(): void {
    try {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
      this.sortItems(); // Re-sort items whenever the sort order changes
    } catch (error) {
      // Log error if toggling sort order fails
      console.error('Error in toggleSortOrder:', error);
    }
  }
}
