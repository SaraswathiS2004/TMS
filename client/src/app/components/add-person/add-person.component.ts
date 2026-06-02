import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { FunctionService } from '../../services/function.service';
import { GuestGroupService } from '../../services/guest-group.service';
import { People } from '../../models/people.model';
import { TmsFunction } from '../../models/function.model';
import { GuestGroup } from '../../models/guest-group.model';
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
  groups: GuestGroup[] = [];

  constructor(
    private peopleService: PeopleService,
    private functionService: FunctionService,
    private guestGroupService: GuestGroupService
  ) {}

  ngOnInit(): void {
    this.loadPeople();
    this.functionService.getAllFunctions().subscribe({
      next: (fns) => { this.functions = fns; }
    });
    this.guestGroupService.getAllGroups().subscribe({
      next: (groups) => { this.groups = groups; }
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
