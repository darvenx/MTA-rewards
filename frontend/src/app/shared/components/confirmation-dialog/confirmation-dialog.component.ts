import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

interface TransferConfirmationData {
  receiverAccountNumber: string;
  amount: number;
  remarks?: string;
}

@Component({
  selector: 'app-confirmation-dialog',
  standalone: false,
  template: `
    <h2 mat-dialog-title>Confirm Transfer</h2>
    <mat-dialog-content>
      <p>You are about to transfer the following:</p>
      <div class="summary">
        <p>
          <strong>To Account:</strong>
          •••• {{ ('' + data.receiverAccountNumber).slice(-4) }}
        </p>
        <p>
          <strong>Amount:</strong>
          {{ data.amount | currency:'INR':'symbol':'1.2-2' }}
        </p>
        <p *ngIf="data.remarks">
          <strong>Remarks:</strong>
          {{ data.remarks }}
        </p>
      </div>
      <p class="helper-text">Please review the details carefully before confirming.</p>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onNoClick()">Cancel</button>
      <button mat-raised-button color="primary" [mat-dialog-close]="true">Confirm</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .summary {
      background-color: #f9fafb;
      padding: 16px;
      border-radius: 8px;
      margin: 12px 0 8px;
      border: 1px solid rgba(148,163,184,0.25);
    }
    p { margin: 8px 0; }
    .helper-text {
      font-size: 0.85rem;
      color: #6b7280;
    }
  `]
})
export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TransferConfirmationData
  ) { }

  onNoClick(): void {
    this.dialogRef.close();
  }
}
