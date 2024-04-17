# NeverLate Planning
**Non Core Feature** (we should NOT work on this until we’re done with everything else)

## MVP (Minimum Viable Product)

### Main Activity
- **Navigation**
  - Side bar to navigate to different fragments

### Logged Out Fragment
- **Login/Signup**
  - [x] Basic login and signup with Gmail
  - [x] Creates profile with Gmail display name, and photo and saves it to database (firebase)
  - [x] Logout functionality
  - [ ] **Profile picture upload + save to database**
  - [x] **User Personalization** (description about me idk that stuff)
  - [x] **Edit profile**

### Friends Fragment
- **Add friends/contacts**
  - [x] Type to add a friend manually by email
  - [x] **Adding friends send friend request**
  - [x] Save friend to database
    - [x] Double check so we cannot add someone we are already friends with
  - [x] Display friends/contacts with basic info (name, pfp)
  - [ ] **Allows for searching friends**
  - [ ] **Allows importing contacts to add friends**

### Events List Fragment
- [x] Displays list of events
- [x] Button to create new event
- [ ] Events connect to data base
- [x] **Allows for filtering by category**
- **Search by event details idk**

### Event Creation Fragment
- **Event Creation**
  - [ ] Create an event with location, date, address, description, time, and duration
  - [ ] Invite friends to event
- **API: Integrate Google Maps API**
  - [x] Make sure we are able to fetch the user’s coordinates before we do anything more and make functions that can do this

### Event Detail Fragment
- [x] Display the event with its basic info (location, date, address, time, and duration)
- [ ] Participating members
  - [ ] Show their location and when they’ll arrive if they leave <now> rather than a message
  - [ ] Notify button
    - [ ] Call/text/notification? We can decide? Or all 3? We can start with just 1 and add the others to reach goals?
- **Event Location Sharing and Mapping**
  - [ ] Share current real-time locations with others and display that on the map
- **Status**
  - [ ] How long it'll take for the user to get to location
  - [ ] Decide mode of transportation
  - [ ] Remaining time until event
  - [ ] Estimated time of arrival
  - [ ] Should detect when we have arrived + add that to our data analysis
- **Event editing functionality**
- **Bookmark event so it shows on top or smt idk**

### Event List Maps Fragment
- [ ] Display all events on the map (+ current location?)
- **Allow filter by <x> days or the closest <x> events**

### General Notifications
- [ ] The encouragement part like sending reminders at set times before events
  - [ ] We can decide when we want to send notifications (ie. the morning on the day of, 3 hours before event, 10 mins before they should leave)

### Data Analysis
- [ ] Punctuality reports based on event attendance and time late
  - [ ] We need to decide how that is generated
- **Badges for users based on their punctuality**

### Locality?
- [ ] For extra credit, we incorporate another language

## Database Schema
### User
- id: String // UUID
- phoneNumber: Number
- firstName: String
- lastName: String
- profilePicture: any // TODO: find a way to store the picture
- email: String
- passwordHash: String // encrypted password
- friends: Array<String> // Array<UUID of a user>
- totalLateTime: Number // for analysis
- totalEarlyTime: Number // for analysis
- **About Me**
- **Setting Config** // store the user's 'setting'
- **Profile** // any other information related to personal profile page

### Event
- id: String // uuid
- name: String
- date: String // '3/26/2023'
- time: String // '14:00'
- active: Boolean // history event or ongoing event?
- address: String
- members: Array<String> // Array<UUID of a user>
- description: String
- duration: Number // 789243724 milliseconds
- category: String // category that is one of Dining, Study, Meeting. It’ll be useful for filtering

### **Message**
- senderId: String // UUID of a user
- receiverId: String // UUID of a user
- content: String // content of the message
- timestamp: Number

```Kotlin
interface IUserDB {
    val id: String // UUID
    val phoneNumber: Long
    val firstName: String
    val lastName: String
    val profilePicture: Any // For storing profile picture, you might need a custom type or a String URL
    val email: String
    val passwordHash: String // Encrypted password
    val friends: List<String> // List of UUIDs of other users
    val totalLateTime: Long // For analysis
    val totalEarlyTime: Long // For analysis
    // Assuming "About Me" and "Setting Config" are strings for simplicity; adjust as necessary
    // ** val aboutMe: String
    // ** val settingConfig: String
    // ** val profile: String // Other information related to personal profile page; adjust type as necessary
}

interface IEventDB {
    val id: String // UUID
    val name: String
    val date: String // '3/26/2023'; consider using a more suitable date type
    val time: String // '14:00'; consider using a more suitable time type
    val active: Boolean // History event or ongoing event?
    val address: String
    val members: List<String> // List of UUIDs of users
    val description: String
    val duration: Long // Duration in milliseconds
    val category: String // Category that is one of Dining, Study, Meeting. Useful for filtering
}

interface IMessageDB {
    val senderId: String // UUID of a user
    val receiverId: String // UUID of a user
    val content: String // Content of the message
    val timestamp: Long // Consider using a date-time type for more precision
}
```
