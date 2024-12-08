import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EmojiButton } from '@joeattardi/emoji-button';

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
  selectedAuthor: string = this.authorList[0];
  announcementList: string[] = [
    'All Users',
    'Administrators',
    'Managers',
    'Employees',
  ];
  selectedAnnouncement: string = this.announcementList[0];
  selectedEmoji: string = '';
  private emojiPicker: any;
  showEmojiPicker = false;
  maxWordLimit = 10;
  wordCount = 0;

  constructor(
    public dialogRef: MatDialogRef<AddAnnouncementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

  ngOnInit(): void {
    const icons = Quill.import('ui/icons');
    icons.undo = `
      <svg viewBox="0 0 18 18">
        <path d="M12 4V1l-5 4.5L12 10V7c3.6 0 5 2.1 5 5 0 3-2 5-5 5-1.9 0-3.4-1-4.5-2.5l-1.4 1.5c1.6 2 3.8 3.5 5.9 3.5 4 0 7-3 7-7s-3-7-7-7z"></path>
      </svg>`;
    icons.redo = `
      <svg viewBox="0 0 18 18">
        <path d="M6 14v3l5-4.5L6 9v3c-3.6 0-5-2.1-5-5 0-3 2-5 5-5 1.9 0 3.4 1 4.5 2.5l1.4-1.5C9.4 2 7.2 0.5 5 0.5 1 0.5-2 3.5-2 7.5s3 7 7 7z"></path>
      </svg>`;
    icons.emoji = `
      <svg width="21" height="21" viewBox="0 0 21 21" fill="none" xmlns="http://www.w3.org/2000/svg">
<mask id="mask0_710_6226" style="mask-type:alpha" maskUnits="userSpaceOnUse" x="0" y="0" width="21" height="21">
<rect x="0.5" y="0.5" width="20" height="20" fill="#D9D9D9"/>
</mask>
<g mask="url(#mask0_710_6226)">
<path d="M13.4167 9.66663C13.764 9.66663 14.0591 9.5451 14.3022 9.30204C14.5452 9.05899 14.6667 8.76385 14.6667 8.41663C14.6667 8.0694 14.5452 7.77426 14.3022 7.53121C14.0591 7.28815 13.764 7.16663 13.4167 7.16663C13.0695 7.16663 12.7744 7.28815 12.5313 7.53121C12.2883 7.77426 12.1667 8.0694 12.1667 8.41663C12.1667 8.76385 12.2883 9.05899 12.5313 9.30204C12.7744 9.5451 13.0695 9.66663 13.4167 9.66663ZM7.58341 9.66663C7.93064 9.66663 8.22578 9.5451 8.46883 9.30204C8.71189 9.05899 8.83341 8.76385 8.83341 8.41663C8.83341 8.0694 8.71189 7.77426 8.46883 7.53121C8.22578 7.28815 7.93064 7.16663 7.58341 7.16663C7.23619 7.16663 6.94105 7.28815 6.698 7.53121C6.45494 7.77426 6.33341 8.0694 6.33341 8.41663C6.33341 8.76385 6.45494 9.05899 6.698 9.30204C6.94105 9.5451 7.23619 9.66663 7.58341 9.66663ZM10.5001 15.0833C11.4445 15.0833 12.3022 14.8159 13.073 14.2812C13.8438 13.7465 14.4029 13.0416 14.7501 12.1666H13.3751C13.0695 12.6805 12.6633 13.0868 12.1563 13.3854C11.6494 13.684 11.0973 13.8333 10.5001 13.8333C9.90286 13.8333 9.35078 13.684 8.84383 13.3854C8.33689 13.0868 7.93064 12.6805 7.62508 12.1666H6.25008C6.5973 13.0416 7.15633 13.7465 7.92716 14.2812C8.698 14.8159 9.55564 15.0833 10.5001 15.0833ZM10.5001 18.8333C9.3473 18.8333 8.26397 18.6145 7.25008 18.177C6.23619 17.7395 5.35425 17.1458 4.60425 16.3958C3.85425 15.6458 3.2605 14.7638 2.823 13.75C2.3855 12.7361 2.16675 11.6527 2.16675 10.5C2.16675 9.34718 2.3855 8.26385 2.823 7.24996C3.2605 6.23607 3.85425 5.35413 4.60425 4.60413C5.35425 3.85413 6.23619 3.26038 7.25008 2.82288C8.26397 2.38538 9.3473 2.16663 10.5001 2.16663C11.6529 2.16663 12.7362 2.38538 13.7501 2.82288C14.764 3.26038 15.6459 3.85413 16.3959 4.60413C17.1459 5.35413 17.7397 6.23607 18.1772 7.24996C18.6147 8.26385 18.8334 9.34718 18.8334 10.5C18.8334 11.6527 18.6147 12.7361 18.1772 13.75C17.7397 14.7638 17.1459 15.6458 16.3959 16.3958C15.6459 17.1458 14.764 17.7395 13.7501 18.177C12.7362 18.6145 11.6529 18.8333 10.5001 18.8333ZM10.5001 17.1666C12.3612 17.1666 13.9376 16.5208 15.2292 15.2291C16.5209 13.9375 17.1667 12.3611 17.1667 10.5C17.1667 8.63885 16.5209 7.06246 15.2292 5.77079C13.9376 4.47913 12.3612 3.83329 10.5001 3.83329C8.63897 3.83329 7.06258 4.47913 5.77091 5.77079C4.47925 7.06246 3.83341 8.63885 3.83341 10.5C3.83341 12.3611 4.47925 13.9375 5.77091 15.2291C7.06258 16.5208 8.63897 17.1666 10.5001 17.1666Z" fill="#00569B"/>
</g>
</svg>
`;
    this.quill = new Quill('#editor', {
      theme: 'snow',
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
            ['clean'],
            ['emoji'],

          ],
          handlers: {
            undo: () => this.undoChange(),
            redo: () => this.redoChange(),
          },
        },
        emoji: true,
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

    this.quill.on('text-change', (delta: any, oldDelta: any, source: string) => {
      if (source === 'user') {
        this.handleWordLimit();
      }
    });

    this.emojiPicker.on('emoji', (selection: any) => {
      this.selectedEmoji = selection.emoji;
      this.showEmojiPicker = !this.showEmojiPicker;
      console.log("this.selectedEmoji----" + this.selectedEmoji);
      console.log("this.showEmojiPicker22----" + this.showEmojiPicker);

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
        this.quill.deleteText(this.quill.getLength() - excessWords, excessWords);
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

  toggleEmojiPicker(event: MouseEvent): void {
    this.emojiPicker.togglePicker(event.target);
    // this.emojiPicker.pickerContainer.style.zIndex = '2000';
    console.log("event----" + event);
    this.showEmojiPicker = false;
    console.log("this.showEmojiPicker----" + this.showEmojiPicker);

  }

  saveAnnouncement(): void {
    this.announcement.content = this.quill.root.innerHTML;
    this.dialogRef.close(this.announcement);
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
