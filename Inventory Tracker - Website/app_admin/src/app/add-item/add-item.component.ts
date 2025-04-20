import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { ItemDataService } from '../services/item-data.service';
import { Item } from '../models/item';

@Component({
  selector: 'app-add-item',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-item.component.html',
  styleUrl: './add-item.component.css',
})
export class AddItemComponent implements OnInit {
  public addForm!: FormGroup;     // Reactive form group for new item input
  submitted = false;              // Flag to track form submission state

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private itemService: ItemDataService
  ) { }

  /**
   * Angular lifecycle hook: initializes the form on component load.
   */
  ngOnInit() {
    try {
      this.addForm = this.formBuilder.group({
        _id: [],                                   // MongoDB document ID (optional)
        code: ['', Validators.required],           // Item code (required)
        name: ['', Validators.required],           // Item name (required)
        quantity: ['', Validators.required],       // Item quantity (required)
      });
    } catch (error) {
      console.error('Error initializing form:', error);
    }
  }

  /**
   * Handles form submission.
   * Validates uniqueness of the item code and adds the item via service.
   */
  public onSubmit() {
    this.submitted = true;

    if (this.addForm.valid) {
      try {
        const newItemName = this.addForm.value.code.trim().toLowerCase();

        // Fetch existing items to validate uniqueness
        this.itemService.getItems().subscribe({
          next: (existingItems: Item[]) => {
            try {
              // Check if item code already exists
              const nameExists = existingItems.some(
                item => item.code.trim().toLowerCase() === newItemName
              );

              if (nameExists) {
                alert('An item with this code already exists!');
              } else {
                // Proceed to add new item
                this.itemService.addItem(this.addForm.value).subscribe({
                  next: (data: any) => {
                    console.log('Item added successfully:', data);
                    this.router.navigate(['']); // Navigate back to item list
                  },
                  error: (error: any) => {
                    console.error('Error adding item:', error);
                  },
                });
              }
            } catch (innerError) {
              console.error('Error validating item code:', innerError);
            }
          },
          error: (error: any) => {
            console.error('Error fetching items:', error);
          },
        });
      } catch (error) {
        console.error('Unexpected error during form submission:', error);
      }
    }
  }

  /**
   * Utility getter for form controls.
   * Helps simplify template validation logic.
   */
  get f() {
    return this.addForm.controls;
  }

  /**
   * Navigates back to the previous page.
   */
  goBack() {
    try {
      window.history.back();
    } catch (error) {
      console.error('Error navigating back:', error);
    }
  }
}
