import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-participants',
  templateUrl: './participants.component.html',
  styleUrls: ['./participants.component.css'] // Corrected 'styleUrl' to 'styleUrls'
})
export class ParticipantsComponent implements OnInit {
  id: string | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    // Fetch the ID from the route parameters
    this.id = this.route?.snapshot?.paramMap?.get('id');

    // Alternatively, subscribe to paramMap for dynamic updates
    this.route.paramMap.subscribe(params => {
      this.id = params.get('id');
    });
  }
}
