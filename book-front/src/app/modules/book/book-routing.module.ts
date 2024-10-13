import {NgModule} from '@angular/core';
import {MainPageComponent} from "./pages/main-page/main-page.component";
import {authGuard} from "../../services/guard/auth.guard";
import {RouterModule} from "@angular/router";
import {BookListPageComponent} from "./pages/book-list-page/book-list-page.component";
import {MyBooksPageComponent} from "./pages/my-books-page/my-books-page.component";
import {ManageBookPageComponent} from "./pages/manage-book-page/manage-book-page.component";
import {BookDetailsPageComponent} from "./pages/book-details-page/book-details-page.component";
import {BorrowedBookPageComponent} from "./pages/borrowed-book-page/borrowed-book-page.component";
import {ReturnedBookPageComponent} from "./pages/returned-book-page/returned-book-page.component";


export const routes = [{
  path: '',
  component: MainPageComponent,
  canActivate: [authGuard],
  children: [
    {
      path: '',
      component: BookListPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'my-books',
      component: MyBooksPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'manage',
      component: ManageBookPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'manage/:bookId',
      component: ManageBookPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'my-borrowed-books',
      component: BorrowedBookPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'my-returned-books',
      component: ReturnedBookPageComponent,
      canActivate: [authGuard]
    },
    {
      path: 'details/:bookId',
      component: BookDetailsPageComponent,
      canActivate: [authGuard]
    }
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BookRoutingModule { }
