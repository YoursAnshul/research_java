import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-participant-id',
  templateUrl: './participant-id.component.html',
  styleUrl: './participant-id.component.css'
})
export class ParticipantIdComponent implements OnInit {
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
