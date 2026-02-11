import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TransferService } from '../../services/transfer.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmationDialogComponent } from '../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { TransferRequest } from '../../core/models/transfer-request.model';

@Component({
    selector: 'app-transfer',
    standalone: false,
    templateUrl: './transfer.component.html',
    styleUrls: ['./transfer.component.css']
})
export class TransferComponent implements OnInit {
    transferForm: FormGroup;
    isLoading = false;
    currentAccountId: string | null = null;

    constructor(
        private fb: FormBuilder,
        private transferService: TransferService,
        private authService: AuthService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private router: Router
    ) {
        this.transferForm = this.fb.group({
            senderAccountNumber: [{ value: '', disabled: true }, Validators.required],
            receiverAccountNumber: ['', Validators.required],
            amount: [null, [Validators.required, Validators.min(0.01)]],
            remarks: ['']
        });
    }

    ngOnInit(): void {
        this.currentAccountId = this.authService.getAccountId();
        if (this.currentAccountId) {
            this.transferForm.patchValue({ senderAccountNumber: this.currentAccountId });
        }
    }

    initiateTransfer(): void {
        if (this.transferForm.valid) {
            // Open Confirmation Dialog
            const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
                width: '400px',
                data: this.transferForm.getRawValue()
            });

            dialogRef.afterClosed().subscribe(result => {
                if (result) {
                    this.executeTransfer();
                }
            });
        }
    }

    executeTransfer(): void {
        this.isLoading = true;
        const formValue = this.transferForm.getRawValue();

        // Generate Idempotency Key
        const idempotencyKey = self.crypto.randomUUID();

        const request: TransferRequest = {
            senderAccountNumber: Number(sessionStorage.getItem("id")),
            receiverAccountNumber: Number(formValue.receiverAccountNumber),
            amount: formValue.amount,
            idempotencyKey: idempotencyKey,
            senderAccountPin:String(formValue.remarks)
        };
        // console.log(request);

        this.transferService.transfer(request).subscribe({
            next: (ok) => {
                this.isLoading = false;
                if (ok) {
                    this.snackBar.open('Transfer Successful!', 'View Dashboard', { duration: 5000 })
                        .onAction().subscribe(() => this.router.navigate(['/dashboard']));
                    this.router.navigate(['/dashboard']);
                    return;
                }
                this.snackBar.open('Transfer Failed', 'Close', { duration: 5000 });
            },
            error: (err) => {
                this.isLoading = false;
                this.snackBar.open('Transfer Failed: ' + (err.error?.message || 'Unknown error'), 'Close', { duration: 5000 });
            }
        });
    }

    goBack(): void {
        this.router.navigate(['/dashboard']);
    }
}
