import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  MatDialogRef,
  MAT_DIALOG_DATA,
  MatDialog,
} from '@angular/material/dialog';
import { EmojiButton } from '@joeattardi/emoji-button';
import { PreviewComponent } from './preview.component';

declare var Quill: any;

@Component({
  selector: 'app-add-announcement-dialog',
  templateUrl: './add-announcement-dialog.component.html',
  styleUrls: ['./add-announcement-dialog.component.css'],
})
export class AddAnnouncementDialogComponent implements OnInit {
  announcement = { title: '', content: '' };
  private quill: any;
  authorList: string[] = ['Hannah Campbell', 'Hannah Campbell'];
  selectedAuthor: string[] = [];
  announcementList: string[] = ['CARRA', 'HERO Together', 'Project Eleven'];
  selectedAnnouncements: string[] = [];
  selectedEmoji: string = '';
  private emojiPicker: any;
  showEmojiPicker = false;
  maxWordLimit = 500;
  wordCount = 0;
  announcementForm!: FormGroup;
  currentTarget!: 'formField' | 'textArea';

  constructor(
    public dialogRef: MatDialogRef<AddAnnouncementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {
    this.announcementForm = this.fb.group({
      title: ['', Validators.required],
      startDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    const icons = Quill.import('ui/icons');
    icons.undo = `
 <svg width="32" height="32" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M16.5 10.25H8.88438L11.1263 8.00875L10.25 7.125L6.5 10.875L10.25 14.625L11.1263 13.7406L8.88625 11.5H16.5C17.4946 11.5 18.4484 11.8951 19.1517 12.5983C19.8549 13.3016 20.25 14.2554 20.25 15.25C20.25 16.2446 19.8549 17.1984 19.1517 17.9017C18.4484 18.6049 17.4946 19 16.5 19H11.5V20.25H16.5C17.8261 20.25 19.0979 19.7232 20.0355 18.7855C20.9732 17.8479 21.5 16.5761 21.5 15.25C21.5 13.9239 20.9732 12.6521 20.0355 11.7145C19.0979 10.7768 17.8261 10.25 16.5 10.25Z" fill="#212529"/>
</svg>

`;

    icons.redo = `
  <svg width="32" height="32" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
<path d="M11.5 10.25H19.1156L16.8737 8.00875L17.75 7.125L21.5 10.875L17.75 14.625L16.8737 13.7406L19.1137 11.5H11.5C10.5054 11.5 9.55161 11.8951 8.84835 12.5983C8.14509 13.3016 7.75 14.2554 7.75 15.25C7.75 16.2446 8.14509 17.1984 8.84835 17.9017C9.55161 18.6049 10.5054 19 11.5 19H16.5V20.25H11.5C10.1739 20.25 8.90215 19.7232 7.96447 18.7855C7.02678 17.8479 6.5 16.5761 6.5 15.25C6.5 13.9239 7.02678 12.6521 7.96447 11.7145C8.90215 10.7768 10.1739 10.25 11.5 10.25Z" fill="#212529"/>
</svg>

`;
    icons.emoji = `
      <svg width="28" height="28" viewBox="0 0 28 28" fill="none" xmlns="http://www.w3.org/2000/svg">
<mask id="mask0_591_22646" style="mask-type:alpha" maskUnits="userSpaceOnUse" x="4" y="4" width="20" height="20">
<rect x="4" y="4" width="20" height="20" fill="#D9D9D9"/>
</mask>
<g mask="url(#mask0_591_22646)">
<path d="M16.9165 13.1667C17.2637 13.1667 17.5589 13.0452 17.8019 12.8021C18.045 12.559 18.1665 12.2639 18.1665 11.9167C18.1665 11.5695 18.045 11.2743 17.8019 11.0313C17.5589 10.7882 17.2637 10.6667 16.9165 10.6667C16.5693 10.6667 16.2741 10.7882 16.0311 11.0313C15.788 11.2743 15.6665 11.5695 15.6665 11.9167C15.6665 12.2639 15.788 12.559 16.0311 12.8021C16.2741 13.0452 16.5693 13.1667 16.9165 13.1667ZM11.0832 13.1667C11.4304 13.1667 11.7255 13.0452 11.9686 12.8021C12.2116 12.559 12.3332 12.2639 12.3332 11.9167C12.3332 11.5695 12.2116 11.2743 11.9686 11.0313C11.7255 10.7882 11.4304 10.6667 11.0832 10.6667C10.7359 10.6667 10.4408 10.7882 10.1978 11.0313C9.9547 11.2743 9.83317 11.5695 9.83317 11.9167C9.83317 12.2639 9.9547 12.559 10.1978 12.8021C10.4408 13.0452 10.7359 13.1667 11.0832 13.1667ZM13.9998 18.5834C14.9443 18.5834 15.8019 18.316 16.5728 17.7813C17.3436 17.2465 17.9026 16.5417 18.2498 15.6667H16.8748C16.5693 16.1806 16.163 16.5868 15.6561 16.8854C15.1491 17.184 14.5971 17.3334 13.9998 17.3334C13.4026 17.3334 12.8505 17.184 12.3436 16.8854C11.8366 16.5868 11.4304 16.1806 11.1248 15.6667H9.74984C10.0971 16.5417 10.6561 17.2465 11.4269 17.7813C12.1978 18.316 13.0554 18.5834 13.9998 18.5834ZM13.9998 22.3334C12.8471 22.3334 11.7637 22.1146 10.7498 21.6771C9.73595 21.2396 8.854 20.6459 8.104 19.8959C7.354 19.1459 6.76025 18.2639 6.32275 17.25C5.88525 16.2361 5.6665 15.1528 5.6665 14C5.6665 12.8472 5.88525 11.7639 6.32275 10.75C6.76025 9.73613 7.354 8.85419 8.104 8.10419C8.854 7.35419 9.73595 6.76044 10.7498 6.32294C11.7637 5.88544 12.8471 5.66669 13.9998 5.66669C15.1526 5.66669 16.2359 5.88544 17.2498 6.32294C18.2637 6.76044 19.1457 7.35419 19.8957 8.10419C20.6457 8.85419 21.2394 9.73613 21.6769 10.75C22.1144 11.7639 22.3332 12.8472 22.3332 14C22.3332 15.1528 22.1144 16.2361 21.6769 17.25C21.2394 18.2639 20.6457 19.1459 19.8957 19.8959C19.1457 20.6459 18.2637 21.2396 17.2498 21.6771C16.2359 22.1146 15.1526 22.3334 13.9998 22.3334ZM13.9998 20.6667C15.8609 20.6667 17.4373 20.0209 18.729 18.7292C20.0207 17.4375 20.6665 15.8611 20.6665 14C20.6665 12.1389 20.0207 10.5625 18.729 9.27085C17.4373 7.97919 15.8609 7.33335 13.9998 7.33335C12.1387 7.33335 10.5623 7.97919 9.27067 9.27085C7.979 10.5625 7.33317 12.1389 7.33317 14C7.33317 15.8611 7.979 17.4375 9.27067 18.7292C10.5623 20.0209 12.1387 20.6667 13.9998 20.6667Z" fill="#212529"/>
</g>
</svg>

`;
    this.quill = new Quill('#editor', {
      theme: 'snow',
      placeholder: 'Enter text...',
      modules: {
        toolbar: {
          container: [
            ['undo', 'redo'],
            [{ header: [1, 2, 3, false] }],
            [{ align: [] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['link', 'blockquote', 'code-block', 'image'],
            [{ color: [] }, { background: [] }],
            ['emoji'],
          ],
          handlers: {
            undo: () => this.undoChange(),
            redo: () => this.redoChange(),
            emoji: (value: any) => {
              const event = new MouseEvent('click', {
                bubbles: true,
                cancelable: true,
              });
              Object.defineProperty(event, 'target', {
                value: document.querySelector('.ql-emoji'),
                enumerable: true,
              });
              this.toggleEmojiPicker(event, 'textArea');
            },
          },
        },
        history: {
          delay: 1000,
          maxStack: 50,
          userOnly: true,
        },
      },
    });

    this.emojiPicker = new EmojiButton();
    if (this.data?.content) {
      this.quill.root.innerHTML = this.data.content;
    }

    this.quill.on(
      'text-change',
      (delta: any, oldDelta: any, source: string) => {
        if (source === 'user') {
          this.handleWordLimit();
        }
      }
    );

    this.emojiPicker.on('emoji', (selection: any) => {
      if (this.currentTarget === 'formField') {
        this.selectedEmoji = selection.emoji;
      } else if (this.currentTarget === 'textArea') {
        const range = this.quill.getSelection();
        if (range) {
          this.quill.insertText(range.index, selection.emoji, 'user');
          this.quill.setSelection(range.index + selection.emoji.length);
        } else {
          this.quill.insertText(
            this.quill.getLength() - 1,
            selection.emoji,
            'user'
          );
        }
      }
      this.emojiPicker.hidePicker();
      this.showEmojiPicker = false;
    });
  }

  handleWordLimit(): void {
    const text = this.quill.getText().trim();
    const words = text.match(/\b\S+\b/g) || [];
    this.wordCount = words.length;

    if (this.wordCount > this.maxWordLimit) {
      const excessWords = this.wordCount - this.maxWordLimit;
      const range = this.quill.getSelection();
      if (range) {
        this.quill.deleteText(range.index - excessWords, excessWords);
      } else {
        this.quill.deleteText(
          this.quill.getLength() - excessWords,
          excessWords
        );
      }
      this.wordCount = this.maxWordLimit;
    }
  }

  undoChange(): void {
    this.quill.history.undo();
  }

  redoChange(): void {
    this.quill.history.redo();
  }

  toggleEmojiPicker(
    event: MouseEvent | { target: HTMLElement },
    target: 'formField' | 'textArea'
  ): void {
    const targetElement = event.target as HTMLElement;
    this.currentTarget = target;
    this.emojiPicker.togglePicker(targetElement);
    this.showEmojiPicker = !this.showEmojiPicker;
    console.log(`Emoji Picker toggled for: ${target}`);
  }

  saveAnnouncement(): void {
    this.announcement.content = this.quill.root.innerHTML;
    this.dialogRef.close(this.announcement);
    if (this.announcementForm?.valid) {
      console.log('Announcement saved:', this.announcementForm.value);
    }
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
  // Open the priview dialog
  openPreview(): void {
    this.closeDialog();
    console.log('Opening preview dialog...');
    const dialogRef = this.dialog.open(PreviewComponent, {
      width: '600px',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        console.log('result-------', result);
      }
    });
  }
  getTagColor(tag: string): string {
    switch (tag) {
      case 'CARRA':
        return '#74EBA4';
      case 'HERO Together':
        return '#F6C867';
      case 'Project Eleven':
        return '#87CEFA';
      default:
        return '#d3d3d3'; 
    }
  }
}
