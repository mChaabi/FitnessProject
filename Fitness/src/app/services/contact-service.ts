import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageContact } from '../models/message-contact';

@Injectable({
  providedIn: 'root'
})
export class ContactService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/contacts/send';

  /**
   * Envoie le formulaire de contact au backend Java
   * @param data Les informations saisies par l'utilisateur
   */
  sendEmail(data: MessageContact): Observable<any> {
    return this.http.post(this.API_URL, data);
  }
envoyerMessage(data: any): Observable<any> {
    return this.http.post(this.API_URL, data);
  }
}
