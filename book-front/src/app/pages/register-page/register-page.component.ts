import { Component } from '@angular/core';

import {Router} from "@angular/router";
import {AuthenticationService} from "../../services/services/authentication.service";
import {FormsModule} from "@angular/forms";
import {RegistrationRequest} from "../../services/models/registration-request";
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-register-page',
  standalone: true,
  templateUrl: './register-page.component.html',
  imports: [
    FormsModule,
    NgIf,
    NgForOf
  ],
  styleUrl: './register-page.component.scss'
})
export class RegisterPageComponent {
  registerRequest: RegistrationRequest = {email: '', firstName: '', lastName: '', password: ''};
  errorMsg: Array<string> = [];

  constructor(
    private router: Router,
    private authService: AuthenticationService
  ) {
  }

  login() {
    this.router.navigate(['login']);
  }

  register() {
    this.errorMsg = [];
    this.authService.register({
      body: this.registerRequest
    })
      .subscribe({
        next: () => {
          this.router.navigate(['activate-account']);
        },
        error: (err) => {
          this.errorMsg = err.error.validationErrors;
        }
      });
  }
}
