import { Component, Input, OnInit } from '@angular/core';
import { IDropDownValue } from '../../interfaces/interfaces';
import { ConfigurationService } from '../../services/configuration/configuration.service';
import { LogsService } from '../../services/logs/logs.service';

@Component({
  selector: 'app-language-icon',
  templateUrl: './language-icon.component.html',
  styleUrls: ['./language-icon.component.css']
})
export class LanguageIconComponent implements OnInit {

  @Input() language!: string;
  languageAbr: string = ''
  languageColor: string = '';
  errorMessage!: string;

  constructor(private configurationService: ConfigurationService,
    private logsService: LogsService) {
  }

  ngOnInit(): void {
    this.configurationService.languages.subscribe(
      languageField => {

        if (languageField) {
          if (languageField.dropDownValues) {
            let languageInfo: IDropDownValue | undefined = languageField.dropDownValues.find(x => (x.codeValues || '').toString().toUpperCase() == this.language.toUpperCase());

            if (languageInfo) {
              this.languageAbr = languageInfo.abbr || '';
              this.languageColor = languageInfo.colorCode || '';
            }
          }
        }
      },
      error => {
        this.errorMessage = <string>(error.message);
        this.logsService.logError(this.errorMessage); console.log(this.errorMessage);
      }
    );
  }

  customStyle() {
    return {
      'font-size': '1em',
      'color': this.languageColor,
      'margin-left': '3px'
    };
  }

}
