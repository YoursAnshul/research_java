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

  constructor(
    public dialogRef: MatDialogRef<AddAnnouncementDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    this.quill = new Quill('#editor', {
      theme: 'snow',
      modules: {
        toolbar: [
          [{ header: [1, 2, 3, false] }],
          ['bold', 'italic', 'underline'],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['link', 'blockquote', 'code-block'],
          [{ align: [] }],
          [{ color: [] }, { background: [] }]
        ]
      }
    });

    this.emojiPicker = new EmojiButton();

    if (this.data?.content) {
      this.quill.root.innerHTML = this.data.content;
    }

    this.emojiPicker.on('emoji', (selection: any) => {
      this.selectedEmoji = selection.emoji;
    });
  }

  toggleEmojiPicker(event: MouseEvent): void {
    this.emojiPicker.togglePicker(event.target);
      this.emojiPicker.pickerContainer.style.zIndex = '2000';
  }

  saveAnnouncement(): void {
    this.announcement.content = this.quill.root.innerHTML;
    this.dialogRef.close(this.announcement);
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}
