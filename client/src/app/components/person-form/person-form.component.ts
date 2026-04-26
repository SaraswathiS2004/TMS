import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PeopleService } from '../../services/people.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-person-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TranslatePipe],
  templateUrl: './person-form.component.html',
  styleUrl: './person-form.component.css'
})
export class PersonFormComponent implements OnInit, OnChanges {

  @Input() person?: People;
  @Input() allPeople: People[] = [];
  @Input() functions: TmsFunction[] = [];
  @Input() inline = false;

  @Output() saved = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  form: FormGroup;
  selectedFunctionIds: number[] = [];

  allNames: string[] = [];
  allCities: string[] = [];
  filteredNames: string[] = [];
  filteredCities: string[] = [];
  showNameDropdown = false;
  showCityDropdown = false;

  successMessage = '';
  errorMessage = '';
  isSubmitting = false;

  get isEditMode(): boolean {
    return !!this.person;
  }

  constructor(private fb: FormBuilder, private peopleService: PeopleService) {
    this.form = this.fb.group({
      name:           ['', [Validators.required, Validators.minLength(2)]],
      city:           ['', Validators.required],
      numberOfPerson: [1,  [Validators.required, Validators.min(1)]],
      relationType:   ['CLOSE_RELATIVE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.updateAutocompleteData();
    if (this.person) {
      this.patchForm();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['allPeople']) {
      this.updateAutocompleteData();
    }
    if (changes['person'] && this.person) {
      this.patchForm();
    }
  }

  private updateAutocompleteData(): void {
    this.allNames  = [...new Set(this.allPeople.map(p => p.name))].sort();
    this.allCities = [...new Set(this.allPeople.map(p => p.city))].sort();
  }

  private patchForm(): void {
    this.form.patchValue({
      name:           this.person!.name,
      city:           this.person!.city,
      numberOfPerson: this.person!.numberOfPerson,
      relationType:   this.person!.relationType
    });
    this.selectedFunctionIds = [...(this.person!.invitedFunctionIds ?? [])];
  }

  hasError(field: string, error: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched && ctrl?.hasError(error));
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
    this.filteredCities = val.length === 0
      ? [...this.allCities]
      : this.allCities.filter(c => c.toLowerCase().includes(val));
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

  toggleFunction(fnId: number): void {
    if (this.selectedFunctionIds.includes(fnId)) {
      this.selectedFunctionIds = this.selectedFunctionIds.filter(id => id !== fnId);
    } else {
      this.selectedFunctionIds = [...this.selectedFunctionIds, fnId];
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSubmitting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const payload: People = { ...this.form.value, invitedFunctionIds: this.selectedFunctionIds };

    if (this.isEditMode) {
      payload.id = this.person!.id;
      this.peopleService.updatePerson(payload).subscribe({
        next: (resp) => {
          this.isSubmitting = false;
          if (resp.status === 'SUCCESS') {
            this.saved.emit();
          } else {
            this.errorMessage = 'addPerson.serverError';
          }
        },
        error: () => {
          this.isSubmitting = false;
          this.errorMessage = 'addPerson.serverError';
        }
      });
    } else {
      this.peopleService.addPerson(payload).subscribe({
        next: (resp) => {
          this.isSubmitting = false;
          if (resp.status === 'SUCCESS') {
            this.successMessage = 'addPerson.success';
            this.form.reset({ numberOfPerson: 1, relationType: 'CLOSE_RELATIVE' });
            this.selectedFunctionIds = [];
            this.saved.emit();
          } else {
            this.errorMessage = 'addPerson.serverError';
          }
        },
        error: () => {
          this.isSubmitting = false;
          this.errorMessage = 'addPerson.serverError';
        }
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
  }
}
