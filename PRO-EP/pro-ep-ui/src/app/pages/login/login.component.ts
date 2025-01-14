import { Component } from '@angular/core';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { AppConfig } from '../../models/app-config';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  redirectLoginUrl: string = '';
  constructor(private configurationService: ConfigurationService) {
    this.configurationService.appConfig.subscribe(
      appConfig => {
        if (appConfig) {
          this.redirectLoginUrl = appConfig.assertionUrl + '?wtrealm=' + encodeURI(appConfig.realm || '') + '&wa=wsignin1.0';
        }
      }
    );
  }
 
  login() {
    console.log('this.redirectLoginUrl : ', this.redirectLoginUrl);
    window.location.href = this.redirectLoginUrl;
  }

}
