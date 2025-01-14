import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { Utils } from '../../classes/utils';
import { IPopupMessage } from '../../interfaces/interfaces';
import { GlobalsService } from '../../services/globals/globals.service';

@Component({
  selector: 'app-modal-popup',
  templateUrl: './modal-popup.component.html',
  styleUrls: ['./modal-popup.component.css']
})
export class ModalPopupComponent implements OnInit {
  @Input() title: string = '';
  @Output() closeEvent = new EventEmitter<boolean>();

  popupMessage!: IPopupMessage;

  //custom popup message handling
  constructor(private globalsService: GlobalsService) {
    this.globalsService.popupMessage.subscribe(
      popupMessage => {
        this.popupMessage = popupMessage;

        if (popupMessage?.title) {
          this.title = popupMessage.title;
        }
      }
    );

  }

  ngOnInit(): void {
    this.openModalPopup();

    //dynamic popup resizing
    Utils.modalPopupDynamicSize();

    window.addEventListener('resize', function (event) {
      Utils.modalPopupDynamicSize();
    });

  }

  ngOnDestroy(): void {
    this.closeModalPopup();
  }

  openModalPopup(): void {
    if (!this.popupMessage.noBlackFilter) {
      this.globalsService.displayBlackFilter();
    }
  }

  closeModalPopup(): void {
    if (typeof this.popupMessage?.closeCallback === 'function') {
      this.popupMessage.closeCallback();
    }

    this.closeEvent.emit(true);
    if (!this.popupMessage.noBlackFilter) {
      this.globalsService.hideBlackFilter();
    }
  }

  //button callback function
  callFunction(callbackFunction: Function | undefined): void {
    if (!callbackFunction) {
      this.closeModalPopup();
      return;
    }

    if (typeof callbackFunction === 'function') {
      callbackFunction();
    }

    this.closeModalPopup();

  }

}
