import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PeopleService } from '../../services/people.service';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-add-person',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslatePipe],
  templateUrl: './add-person.component.html',
  styleUrl: './add-person.component.css'
})
export class AddPersonComponent implements OnInit {

  form: FormGroup;

  allNames: string[] = [];
  allCities: string[] = [];
  filteredNames: string[] = [];
  filteredCities: string[] = [];
  showNameDropdown = false;
  showCityDropdown = false;

  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  constructor(private fb: FormBuilder, private peopleService: PeopleService) {
    this.form = this.fb.group({
      name:           ['', [Validators.required, Validators.minLength(2)]],
      city:           ['', Validators.required],
      numberOfPerson: [1, [Validators.required, Validators.min(1)]],
      relationType:   ['CLOSE_RELATIVE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.peopleService.getAllPeople().subscribe({
      next: (people) => {
        this.allNames  = [...new Set(people.map(p => p.name))].sort();
        this.allCities = [...new Set(people.map(p => p.city))].sort();
      }
    });
  }

  hasError(field: string, error: string): boolean {
    const control = this.form.get(field);
    return !!(control?.invalid && control?.touched && control?.hasError(error));
  }

  onNameInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredNames = val.length === 0 ? [] : this.allNames.filter(n => n.toLowerCase().includes(val));
    this.showNameDropdown = this.filteredNames.length > 0;
  }

  selectName(name: string): void {
    this.form.patchValue({ name });
    this.showNameDropdown = false;
  }

  hideNameDropdown(): void {
    setTimeout(() => { this.showNameDropdown = false; }, 150);
  }

  onCityInput(event: Event): void {
    const val = (event.target as HTMLInputElement).value.toLowerCase();
    this.filteredCities = val.length === 0 ? [...this.allCities] : this.allCities.filter(c => c.toLowerCase().includes(val));
    this.showCityDropdown = this.filteredCities.length > 0;
  }

  showAllCities(): void {
    this.filteredCities = [...this.allCities];
    this.showCityDropdown = this.filteredCities.length > 0;
  }

  selectCity(city: string): void {
    this.form.patchValue({ city });
    this.showCityDropdown = false;
  }

  hideCityDropdown(): void {
    setTimeout(() => { this.showCityDropdown = false; }, 150);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload = { ...this.form.value, invitedFunctionIds: [] };

    this.peopleService.addPerson(payload).subscribe({
      next: (response) => {
        if (response.status === 'SUCCESS') {
          this.successMessage = 'addPerson.success';
          this.form.reset({ numberOfPerson: 1, relationType: 'CLOSE_RELATIVE' });
          this.peopleService.getAllPeople().subscribe({
            next: (people) => {
              this.allNames  = [...new Set(people.map(p => p.name))].sort();
              this.allCities = [...new Set(people.map(p => p.city))].sort();
            }
          });
        } else {
          this.errorMessage = 'addPerson.serverError';
        }
        this.isSubmitting = false;
      },
      error: () => {
        this.errorMessage = 'addPerson.serverError';
        this.isSubmitting = false;
      }
    });
  }
}
