import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {skipUntil} from 'rxjs';
import {AuthenticationService} from "../../services/services/authentication.service";
import {CodeInputModule} from "angular-code-input";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-activate-account-page',
  standalone: true,
  templateUrl: './activate-account-page.component.html',
  imports: [
    CodeInputModule,
    NgIf
  ],
  styleUrl: './activate-account-page.component.scss'
})
export class ActivateAccountPageComponent {


  message = '';
  isOkay = true;
  submitted = false;
  constructor(
    private router: Router,
    private authService: AuthenticationService
  ) {}

  private confirmAccount(token: string) {
    this.authService.confirm({
      token
    }).subscribe({
      next: () => {
        this.message = 'Your account has been successfully activated.\nNow you can proceed to login';
        this.submitted = true;
      },
      error: () => {
        this.message = 'Token has been expired or invalid';
        this.submitted = true;
        this.isOkay = false;
      }
    });
  }

  redirectToLogin() {
    this.router.navigate(['login']);
  }

  onCodeCompleted(token: string) {
    this.confirmAccount(token);
  }

  protected readonly skipUntil = skipUntil;
}
