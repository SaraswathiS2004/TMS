export type RelationType = 'CLOSE_RELATIVE' | 'DISTANCE_RELATIVE' | 'FRIENDS';

export interface InvitationPerson {
  id?: number;
  name: string;
  note?: string;
}

export interface People {
  id?: number;
  name: string;
  city: string;
  numberOfPerson: number;
  relationType: RelationType;
  invitedFunctionIds: number[];
  // key is functionId as string (JSON map keys are always strings)
  functionStatuses?: { [fnId: string]: string };
  // named persons (guests) listed under this invitation
  persons?: InvitationPerson[];
  // server-computed convenience field = max(numberOfPerson, persons.length)
  effectiveCount?: number;
}

export interface ApiMessage {
  status: 'SUCCESS' | 'FAIL';
  message: string;
}

/**
 * People expected to attend: the larger of the manually entered count and the number
 * of named persons added to the invitation. Use this everywhere a count is shown/summed.
 */
export function effectivePersonCount(p: People): number {
  return Math.max(p.numberOfPerson ?? 0, p.persons?.length ?? 0);
}
