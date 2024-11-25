import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

@Pipe({
    name: 'highlight'
})
export class HighlightSearch implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) {}

    transform(value: any, searchTerms: string): any {
        if (!searchTerms) {
            return value;
        }

        const re = new RegExp(searchTerms, 'gi'); 
        const highlighted = value?.replace(re, (match:string) => {
            return `<mark style="background-color: yellow; color: black;" >${match}</mark>`;
        });

        return this.sanitizer.bypassSecurityTrustHtml(highlighted);
    }
}