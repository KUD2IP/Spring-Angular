import { Component } from '@angular/core';
import {MenuComponent} from "../../components/menu/menu.component";
import {RouterOutlet} from "@angular/router";

@Component({
  selector: 'app-main-page',
  standalone: true,
  imports: [
    MenuComponent,
    RouterOutlet
  ],
  templateUrl: './main-page.component.html',
  styleUrl: './main-page.component.scss'
})
export class MainPageComponent {

}
