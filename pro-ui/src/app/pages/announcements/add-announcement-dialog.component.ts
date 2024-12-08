import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
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
  announcementForm!: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<AddAnnouncementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder
  ) {
    this.announcementForm = this.fb.group({
      title: ['', Validators.required],
      startDate: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.quill = new Quill('#editor', {
      theme: 'snow',
      modules: {
        toolbar: {
          container: [
            [{ header: [1, 2, 3, false] }],
            [{ align: [] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ list: 'ordered' }, { list: 'bullet' }],
            ['link', 'blockquote', 'code-block', 'image'],
            [{ color: [] }, { background: [] }],
            ['clean'],
            ['emoji'],
            ['undo', 'redo'],
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

    this.quill.on(
      'text-change',
      (delta: any, oldDelta: any, source: string) => {
        if (source === 'user') {
          this.handleWordLimit();
        }
      }
    );

    this.emojiPicker.on('emoji', (selection: any) => {
      this.selectedEmoji = selection.emoji;
      this.showEmojiPicker = !this.showEmojiPicker;
      console.log('this.selectedEmoji----' + this.selectedEmoji);
      console.log('this.showEmojiPicker22----' + this.showEmojiPicker);
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

  toggleEmojiPicker(event: MouseEvent): void {
    this.emojiPicker.togglePicker(event.target);
    // this.emojiPicker.pickerContainer.style.zIndex = '2000';
    console.log('event----' + event);
    this.showEmojiPicker = false;
    console.log('this.showEmojiPicker----' + this.showEmojiPicker);
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
}
