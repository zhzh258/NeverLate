# Quick Start
1. `git clone`
2. Create a file named `secrets.properties` in project root directory.
3. In the `secrets.properties`, add
   
   ```text
   MAPS_API_KEY=REPLACE_WITH_YOUR_API_KEY
   ```
   Replace `REPLACE_WITH_YOUR_API_KEY` with your Google Cloud Platform API Key
   - The following APIs should be enabled:
     - Google Directions
     - Google Places
     - Google Map
4. Build with Gradle and run with emulator

# Demo
https://drive.google.com/file/d/1WYRKkBtSEovG3IaH8yr4UvEkGqYgdK2g/view?usp=sharing

# Planning
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

# Database Schema
### User
```
interface IUser {
    val userId: String // UUID
    val phoneNumber: String
    val firstName: String
    val lastName: String
    val displayName: String
    val photoURL: String // For storing profile picture, you might need a custom type or a String URL
    val email: String
    val passwordHash: String // Encrypted password
    val friends: List<String> // List of UUIDs of other users
    val totalLateTime: Long // For analysis
    val totalEarlyTime: Long // For analysis
    val status: String //status is aboutMe
    val address: String
    val personalSignature: String
    val rate: Int
    val friendRequests: List<String>
}

data class User(
    override val userId: String = "",
    override val phoneNumber: String = "",
    override val firstName: String = "",
    override val lastName: String = "",
    override val displayName: String = "",
    override val photoURL: String = "",
    override val email: String = "",
    override val passwordHash: String = "",
    override val friends: List<String> = emptyList(),
    override val totalLateTime: Long = 0L,
    override val totalEarlyTime: Long = 0L,
    override val status: String = "I'm sleepy",
    override val address: String = "in the middle of Charles River",
    override val personalSignature: String = "It's all about the Mindset",
    override val rate: Int = 5,
    override val friendRequests: List<String> = emptyList()
) : IUser
```

### Event
```Kotlin
data class MemberStatus(
    val id: String = "",
    val arrived: Boolean = false,
    val status: String = "",
    val arriveTime: Long = 0L
)

interface IEvent {
    val active: Boolean // History event or ongoing event?
    val address: String // Think about the format/how to check if it is accurate
    val category: String // Category that is one of Dining, Study, Meeting. Useful for filtering
    val date: Timestamp // '3/26/2023'; consider using a more suitable date type
    val description: String
    val duration: Long // Duration in milliseconds
    val id: String // UUID
    val members: List<MemberStatus> // List of members userid
    val name: String
    val location: GeoPoint
    val photoURL: String // For storing profile picture, you might need a custom type or a String URL
}

data class Event(
    override var active: Boolean = true,
    override var address: String = "",
    override var category: String = "",
    override var date: Timestamp = Timestamp(Date()),
    override var description: String = "",
    override var duration: Long = 0L,
    override var id: String = "",
    override var members: List<MemberStatus> = emptyList(),
    override var name: String = "",
    // Chicago. If you see Chicago on map it means something is wrong
    override var location: GeoPoint = GeoPoint(41.8781, 87.6298),
    override var photoURL: String = "",
) : IEvent
```


### **Message**
- senderId: String // UUID of a user
- receiverId: String // UUID of a user
- content: String // content of the message
- timestamp: Number

```Kotlin
data class Message(
    val messageId: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis() // timestamp for when the message was sent mayb
)
```

