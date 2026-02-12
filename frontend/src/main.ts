import { bootstrapApplication } from '@angular/platform-browser';
import { importProvidersFrom } from '@angular/core';
import { AppComponent } from './app/app.component';
import { AppModule } from './app/app.module';

bootstrapApplication(AppComponent, {
  providers: [
    // Reuse the existing NgModule's providers (interceptors, HttpClientModule, router, animations, etc.)
    importProvidersFrom(AppModule)
  ]
}).catch((err: unknown) => console.error(err));
