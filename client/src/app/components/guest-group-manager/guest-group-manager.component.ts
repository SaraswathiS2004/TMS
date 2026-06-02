import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { GuestGroupService } from '../../services/guest-group.service';
import { GuestGroup } from '../../models/guest-group.model';
import { TranslatePipe } from '../../pipes/translate.pipe';
import { TranslateService } from '../../services/translate.service';

@Component({
  selector: 'app-guest-group-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslatePipe],
  templateUrl: './guest-group-manager.component.html',
  styleUrl: './guest-group-manager.component.css'
})
export class GuestGroupManagerComponent implements OnInit {

  groups: GuestGroup[] = [];
  isLoading = true;

  newName = '';
  newColor = '#4f46e5';
  nameError = false;
  isSaving = false;

  editGroupId: number | null = null;
  editData: { name: string; color: string; displayOrder: number } = { name: '', color: '#4f46e5', displayOrder: 0 };
  deleteConfirmId: number | null = null;

  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  constructor(
    private guestGroupService: GuestGroupService,
    private translateService: TranslateService
  ) {}

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading = true;
    this.editGroupId = null;
    this.deleteConfirmId = null;
    this.guestGroupService.getAllGroups().subscribe({
      next: (groups) => { this.groups = groups; this.isLoading = false; },
      error: () => { this.isLoading = false; }
    });
  }

  saveGroup(): void {
    if (!this.newName.trim()) {
      this.nameError = true;
      return;
    }
    this.nameError = false;
    this.isSaving = true;
    this.guestGroupService.addGroup({
      name: this.newName.trim(),
      color: this.newColor,
      displayOrder: this.groups.length
    }).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'guestGroups.addSuccess' : 'guestGroups.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') {
          this.newName = '';
          this.newColor = '#4f46e5';
          this.loadAll();
        }
        this.isSaving = false;
      },
      error: () => { this.showFeedback('guestGroups.saveError', 'error'); this.isSaving = false; }
    });
  }

  startEdit(group: GuestGroup): void {
    this.editGroupId = group.id;
    this.editData = { name: group.name, color: group.color, displayOrder: group.displayOrder };
    this.deleteConfirmId = null;
  }

  cancelEdit(): void {
    this.editGroupId = null;
  }

  saveEdit(): void {
    if (this.editGroupId === null || !this.editData.name.trim()) { return; }
    this.guestGroupService.updateGroup({
      id: this.editGroupId,
      name: this.editData.name.trim(),
      color: this.editData.color,
      displayOrder: this.editData.displayOrder
    }).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'guestGroups.editSuccess' : 'guestGroups.saveError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        if (resp.status === 'SUCCESS') { this.loadAll(); }
      },
      error: () => { this.showFeedback('guestGroups.saveError', 'error'); }
    });
  }

  requestDelete(id: number): void {
    this.deleteConfirmId = id;
    this.editGroupId = null;
  }

  cancelDelete(): void {
    this.deleteConfirmId = null;
  }

  confirmDelete(id: number): void {
    this.guestGroupService.deleteGroup(id).subscribe({
      next: (resp) => {
        this.showFeedback(resp.status === 'SUCCESS' ? 'guestGroups.deleteSuccess' : 'guestGroups.deleteError',
          resp.status === 'SUCCESS' ? 'success' : 'error');
        this.loadAll();
      },
      error: () => { this.showFeedback('guestGroups.deleteError', 'error'); }
    });
  }

  private showFeedback(key: string, type: 'success' | 'error'): void {
    this.feedbackMessage = this.translateService.translate(key);
    this.feedbackType = type;
    setTimeout(() => { this.feedbackMessage = ''; }, 3000);
  }
}
