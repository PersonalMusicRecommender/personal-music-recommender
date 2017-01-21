import requests
import base64

def request_token():
  CLIENT_ID = '8de267b03c464274a3546bfe84496696'
  CLIENT_SECRET= '11ac7098025545af90be092fe2dd029c'

  AUTHORIZATION_URL = 'https://accounts.spotify.com/api/token'
  PARAMETERS = {'grant_type': 'client_credentials'}
  AUTH_HEADERS = {'AUTHORIZATION': 'Basic ' + base64.b64encode(CLIENT_ID + ':' + CLIENT_SECRET)}

  print 'Requesting token'
  r = requests.post(AUTHORIZATION_URL, data = PARAMETERS, headers = AUTH_HEADERS)
  return r.json()['access_token']

def get_spotify_ids():
  print 'Requesting rated tracks'
  URL = 'http://localhost:9000/get-tracks'
  r = requests.get(URL)
  return r.json()

def get_audio_analysis(token, spotify_id):
  HEADERS = {'AUTHORIZATION': 'Bearer ' + token}
  URL = 'https://api.spotify.com/v1/audio-analysis/' + spotify_id

  print 'Requesting audio analysis for ' + spotify_id
  r = requests.get(URL, headers = HEADERS)
  return r.json()['track']


token = request_token()
tracks = get_rated_tracks()

dataset = open('dataset.txt', 'w')
for track in tracks:
  audio_analysis = get_audio_analysis(token, track['spotify-id'])
  dataset.write(audio_analysis['num_samples'] + ',' + audio_analysis['duration'] + ',' + audio_analysis['loudness'] + ',' + audio_analysis['tempo'] + ',' + audio_analysis['tempo_confidence'] + ',' + audio_analysis['time_signature'] + ',' + audio_analysis['time_signature_confidence'] + ',' + audio_analysis['key'] + ',' + audio_analysis['key_confidence'] + ',' + audio_analysis['mode'] + ',' + audio_analysis['mode_confidence'] + ',' + track['stars'] + '\n')
  dataset.flush()
dataset.close()
