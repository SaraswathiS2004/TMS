import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PeopleService } from '../../services/people.service';
import { People } from '../../models/people.model';
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

  constructor(private peopleService: PeopleService) {}

  ngOnInit(): void {
    this.loadPeople();
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
