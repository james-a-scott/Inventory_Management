import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { ItemDataService } from '../services/item-data.service';
import { Item } from '../models/item';

@Component({
  selector: 'app-edit-item',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-item.component.html',
  styleUrl: './edit-item.component.css',
})
export class EditItemComponent implements OnInit {
  public editForm!: FormGroup; // Reactive form instance
  item!: Item;                 // Item object to hold form data
  submitted = false;           // Tracks if form has been submitted
  message: string = '';        // Message for user feedback

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private route: ActivatedRoute,
    private itemDataService: ItemDataService
  ) { }

  /**
   * Lifecycle hook to initialize the component.
   * Retrieves item code from route, initializes form, and fetches item data.
   */
  ngOnInit(): void {
    try {
      // Retrieve item code from the route parameter
      const itemCode = this.route.snapshot.paramMap.get('code');

      // If no code is provided, alert and redirect
      if (!itemCode) {
        alert('Error: No item code found!');
        this.router.navigate(['']);
        return;
      }

      console.log('EditItemComponent::ngOnInit - itemCode:', itemCode);

      // Initialize the reactive form with validation rules
      this.editForm = this.formBuilder.group({
        _id: [''],
        code: [itemCode, Validators.required],
        name: ['', Validators.required],
        quantity: ['', [Validators.required, Validators.min(0)]], // Enforce non-negative values
      });

      // Fetch item data from backend using item code
      this.itemDataService.getItem(itemCode).subscribe({
        next: (response: any) => {
          if (response.length > 0) {
            this.item = response[0];

            // Convert quantity to number to avoid NaN issues
            this.item.quantity = Number(this.item.quantity) || 0;

            // Populate form with item data
            this.editForm.patchValue(this.item);
          } else {
            this.message = 'Item not found!';
          }
          console.log(this.message);
        },
        error: (error: any) => {
          console.error('Error retrieving item:', error);
        }
      });
    } catch (error) {
      console.error('Unexpected error during ngOnInit:', error);
    }
  }

  /**
   * Handler for form submission.
   * Validates form and updates the item through the data service.
   */
  public onSubmit(): void {
    try {
      this.submitted = true;

      // Check if the form is valid before proceeding
      if (this.editForm.valid) {
        const updatedItem = this.editForm.value;

        // Ensure quantity is stored as a number
        updatedItem.quantity = Number(updatedItem.quantity);

        // Submit the update to the backend
        this.itemDataService.updateItem(updatedItem).subscribe({
          next: () => {
            console.log('Item updated successfully');
            this.router.navigate(['']); // Navigate back to main item list
          },
          error: (error: any) => {
            console.error('Error updating item:', error);
          }
        });
      }
    } catch (error) {
      console.error('Unexpected error during onSubmit:', error);
    }
  }

  // Shortcut to form controls for easier access in the template
  get f() {
    return this.editForm.controls;
  }


  /**
   * Navigates the user back to the previous page using browser history.
   */
  goBack(): void {
    try {
      window.history.back();
    } catch (error) {
      console.error('Error going back:', error);
    }
  }
}
