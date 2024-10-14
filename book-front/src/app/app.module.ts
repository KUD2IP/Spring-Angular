import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {bootstrapApplication, BrowserModule} from "@angular/platform-browser";
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {CodeInputModule} from "angular-code-input";
import {ApiModule} from "./services/api.module";
import {HttpTokenInterceptor} from "./services/interceptor/http-token.interceptor";
import {AppRoutingModule} from "./app-routing.module";
import {AppComponent} from "./app.component";
import {ToastrModule} from "ngx-toastr";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";


@NgModule({
  declarations: [

  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    CodeInputModule,
    ToastrModule.forRoot(),
    ApiModule.forRoot({rootUrl: 'http://localhost:8088/api/v1/'})
  ],
  providers: [
    HttpClient,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpTokenInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
