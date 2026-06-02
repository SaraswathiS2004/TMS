export interface TmsFunction {
  id: number;
  name: string;
  color: string;
  displayOrder: number;
  eventDate?: string | null;  // ISO yyyy-MM-dd
}
