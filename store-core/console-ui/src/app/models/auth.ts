export interface SignUpRequest {
  readonly name: string;
  readonly email: string;
  readonly password: string;
  readonly organization: string;
}

export interface AuthStory {
  readonly heading: string;
  readonly copy: string;
  readonly points: readonly string[];
}
