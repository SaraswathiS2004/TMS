import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PeopleService } from '../../services/people.service';

@Component({
  selector: 'app-add-person',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './add-person.component.html',
  styleUrl: './add-person.component.css'
})
export class AddPersonComponent implements OnInit {

  form: FormGroup;

  // Stores unique cities from existing records for the autocomplete datalist
  existingCities: string[] = [];

  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(private fb: FormBuilder, private peopleService: PeopleService) {
    // Set up the form with default values and validation rules
    this.form = this.fb.group({
      name:           ['', [Validators.required, Validators.minLength(2)]],
      city:           ['', Validators.required],
      numberOfPerson: [1, [Validators.required, Validators.min(1)]],
      relationType:   ['CLOSE', Validators.required],
      invitedStatus:  ['NOT_INVITED']
    });
  }

  ngOnInit(): void {
    // Load all existing people so we can extract city suggestions
    this.peopleService.getAllPeople().subscribe({
      next: (people) => {
        // Use a Set to remove duplicate city names
        this.existingCities = [...new Set(people.map(p => p.city))];
      }
    });
  }

  // Helper used in the template to check if a field has a validation error
  hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched && control?.hasError(error));
  }

  onSubmit(): void {
    if (this.form.invalid) {
      // Mark all fields as touched so error messages show up
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.peopleService.addPerson(this.form.value).subscribe({
      next: (response) => {
        if (response.status === 'SUCCESS') {
          this.successMessage = 'Person added successfully to the invitation list!';
          // Reset form to defaults, keeping relation and status values
          this.form.reset({ numberOfPerson: 1, relationType: 'CLOSE', invitedStatus: 'NOT_INVITED' });
        } else {
          this.errorMessage = response.message || 'Could not add person. Please try again.';
        }
        this.isSubmitting = false;
      },
      error: () => {
        this.errorMessage = 'Server error. Make sure the backend is running.';
        this.isSubmitting = false;
      }
    });
  }
}
