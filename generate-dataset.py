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

def get_track_ids():
  URL = ''
  r = requests.get(URL, headers = headers)


token = request_token()
headers = {'AUTHORIZATION': 'Bearer ' + token}
url = 'https://api.spotify.com/v1/audio-analysis/{}'
