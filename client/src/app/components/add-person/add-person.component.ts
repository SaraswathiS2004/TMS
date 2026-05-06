import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { PersonFormComponent } from '../person-form/person-form.component';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-add-person',
  standalone: true,
  imports: [CommonModule, PersonFormComponent, TranslatePipe],
  templateUrl: './add-person.component.html',
  styleUrl: './add-person.component.css'
})
export class AddPersonComponent implements OnInit {

  allPeople: People[] = [];
  functions: TmsFunction[] = [];

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService
  ) {}

  ngOnInit(): void {
    this.loadPeople();
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => { this.functions = fns; }
    });
  }

  loadPeople(): void {
    this.peopleService.getAllPeople().subscribe({
      next: (people) => { this.allPeople = people; }
    });
  }

  onSaved(): void {
    this.loadPeople();
  }
}
