package com.example.data

import com.example.model.Persona
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object PersonaGenerator {

  private val countryData = mapOf(
    "EG" to CountryProfile(
      name = "Egypt",
      firstNames = listOf("Ahmed", "Mohamed", "Mahmoud", "Omar", "Youssef", "Tarek", "Mostafa", "Karim", "Hassan", "Ali", "Nour", "Fatima", "Mariam", "Salma", "Mona", "Aya", "Sara", "Dina"),
      lastNames = listOf("Al-Masri", "Ibrahim", "Hassan", "El-Sayed", "Mansour", "Abdelrahman", "Farag", "Soliman", "Kamel", "Zaher", "Ghoneim", "Sharaf", "Radwan", "Badawi"),
      cities = listOf("Cairo", "Alexandria", "Giza", "Mansoura", "Tanta", "Port Said", "Ismailia", "Zagazig"),
      streets = listOf("Street 15, Al Maadi", "El Horreya Ave", "Tahrir Square St", "Abbas El Akkad St", "Makram Ebeid", "Zamalek 26th July", "El Nasr Road", "Corniche El Nil"),
      zipCodes = listOf("11511", "11728", "21500", "12511", "35511", "41511"),
      phonePrefix = "+20 10",
      timezone = "Africa/Cairo (GMT+2)",
      language = "ar-EG, ar;q=0.9, en-US;q=0.8"
    ),
    "US" to CountryProfile(
      name = "United States",
      firstNames = listOf("James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Emma", "Olivia", "Ava", "Sophia", "Isabella", "Mia", "Charlotte", "Amelia"),
      lastNames = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson"),
      cities = listOf("New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio", "San Diego", "Dallas", "Austin"),
      streets = listOf("742 Evergreen Terrace", "123 Main Street", "456 Oak Avenue", "789 Pine Street", "101 Maple Blvd", "202 Elm Street", "303 Washington St"),
      zipCodes = listOf("10001", "90001", "60601", "77001", "85001", "19101", "78201", "92101"),
      phonePrefix = "+1 312",
      timezone = "America/New_York (GMT-5)",
      language = "en-US, en;q=0.9"
    ),
    "GB" to CountryProfile(
      name = "United Kingdom",
      firstNames = listOf("Oliver", "George", "Arthur", "Noah", "Muhammad", "Leo", "Harry", "Oscar", "Olivia", "Amelia", "Isla", "Ava", "Mia", "Ivy", "Lily", "Freya"),
      lastNames = listOf("Smith", "Jones", "Taylor", "Brown", "Williams", "Wilson", "Johnson", "Davies", "Robinson", "Wright", "Thompson", "Evans", "Walker", "White", "Roberts"),
      cities = listOf("London", "Manchester", "Birmingham", "Leeds", "Glasgow", "Liverpool", "Newcastle", "Sheffield", "Bristol", "Edinburgh"),
      streets = listOf("10 Downing Street", "221B Baker Street", "45 Oxford Road", "12 High Street", "78 Victoria Road", "99 King's Way", "14 Queen Street"),
      zipCodes = listOf("EC1A 1BB", "W1A 0AX", "M1 1AE", "B1 1AA", "LS1 1BA", "G1 1XQ"),
      phonePrefix = "+44 79",
      timezone = "Europe/London (GMT+0)",
      language = "en-GB, en;q=0.9"
    ),
    "FR" to CountryProfile(
      name = "France",
      firstNames = listOf("Gabriel", "Léo", "Raphaël", "Louis", "Arthur", "Jules", "Lucas", "Adam", "Jade", "Louise", "Emma", "Alice", "Ambre", "Lina", "Rose", "Chloé"),
      lastNames = listOf("Martin", "Bernard", "Thomas", "Petit", "Robert", "Richard", "Durand", "Dubois", "Moreau", "Laurent", "Simon", "Michel", "Lefebvre", "Leroy", "Roux"),
      cities = listOf("Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Strasbourg", "Montpellier", "Bordeaux", "Lille"),
      streets = listOf("15 Rue de Rivoli", "8 Avenue des Champs-Élysées", "24 Boulevard Saint-Germain", "12 Rue de la Paix", "45 Rue Victor Hugo"),
      zipCodes = listOf("75001", "13001", "69001", "31000", "06000", "44000", "67000"),
      phonePrefix = "+33 6",
      timezone = "Europe/Paris (GMT+1)",
      language = "fr-FR, fr;q=0.9, en-US;q=0.8"
    ),
    "SA" to CountryProfile(
      name = "Saudi Arabia",
      firstNames = listOf("Fahad", "Saud", "Abdullah", "Sultan", "Bandar", "Turki", "Nawaf", "Salman", "Reem", "Noura", "Lama", "Sara", "Hessah", "Maha", "Haya"),
      lastNames = listOf("Al-Otaibi", "Al-Qahtani", "Al-Zahrani", "Al-Ghamdi", "Al-Shehri", "Al-Harbi", "Al-Mutairi", "Al-Dossari", "Al-Anazi", "Al-Shammari"),
      cities = listOf("Riyadh", "Jeddah", "Dammam", "Mecca", "Medina", "Khobar", "Tabuk", "Abha"),
      streets = listOf("King Fahd Road", "Olaya Street", "Tahlia Street", "Prince Sultan Rd", "King Abdullah Rd", "Corniche Road"),
      zipCodes = listOf("11564", "21442", "31411", "24231", "41411"),
      phonePrefix = "+966 5",
      timezone = "Asia/Riyadh (GMT+3)",
      language = "ar-SA, ar;q=0.9, en-US;q=0.8"
    ),
    "DE" to CountryProfile(
      name = "Germany",
      firstNames = listOf("Noah", "Matteo", "Leon", "Finn", "Paul", "Elias", "Emil", "Lukas", "Emilia", "Mia", "Sophia", "Emma", "Hannah", "Lina", "Ella"),
      lastNames = listOf("Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann", "Schäfer", "Koch", "Bauer"),
      cities = listOf("Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne", "Stuttgart", "Düsseldorf", "Leipzig"),
      streets = listOf("Friedrichstraße 42", "Kurfürstendamm 105", "Maximilianstraße 12", "Königsallee 88", "Zeil 55"),
      zipCodes = listOf("10115", "80331", "60311", "20095", "50667", "70173"),
      phonePrefix = "+49 17",
      timezone = "Europe/Berlin (GMT+1)",
      language = "de-DE, de;q=0.9, en-US;q=0.8"
    )
  )

  data class CountryProfile(
    val name: String,
    val firstNames: List<String>,
    val lastNames: List<String>,
    val cities: List<String>,
    val streets: List<String>,
    val zipCodes: List<String>,
    val phonePrefix: String,
    val timezone: String,
    val language: String
  )

  private val emailPool = mutableListOf(
    "ahmed.elmasri992@gmail.com",
    "omar.tarek.cpa88@outlook.com",
    "salma.ibrahim.eg@yahoo.com",
    "youssef.farag.promo@gmail.com",
    "nour.mansour71@hotmail.com",
    "mostafa.ghoneim.lead@gmail.com",
    "dina.soliman94@outlook.com",
    "karim.sharaf.eg@gmail.com"
  )

  private val usedEmails = mutableSetOf<String>()

  fun getAvailableCountries(): List<Pair<String, String>> {
    return countryData.map { (code, profile) -> code to profile.name }
  }

  fun getCountryProfile(code: String): CountryProfile {
    return countryData[code] ?: countryData["EG"]!!
  }

  fun generatePersona(countryCode: String = "EG", customEmail: String? = null): Persona {
    val profile = countryData[countryCode] ?: countryData["EG"]!!
    val firstName = profile.firstNames.random()
    val lastName = profile.lastNames.random()
    val city = profile.cities.random()
    val street = profile.streets.random()
    val zipCode = profile.zipCodes.random()
    val phone = "${profile.phonePrefix} ${Random.nextInt(100, 999)} ${Random.nextInt(1000, 9999)}"

    // Generate birth date (Age between 21 and 55)
    val age = Random.nextInt(22, 52)
    val birthYear = 2026 - age
    val birthMonth = Random.nextInt(1, 13)
    val birthDay = Random.nextInt(1, 29)
    val birthDate = String.format(Locale.US, "%02d/%02d/%d", birthDay, birthMonth, birthYear)

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val monthShorts = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val birthMonthName = monthNames[birthMonth - 1]
    val birthMonthShort = monthShorts[birthMonth - 1]

    val areaCode = "${Random.nextInt(200, 999)}"
    val localPhone = "${Random.nextInt(200, 999)}-${Random.nextInt(1000, 9999)}"
    val isMale = Random.nextBoolean()

    // Pull next available email from pool (without modifying until cycle ends)
    val email = customEmail ?: getNextActiveEmail()

    // Generate valid Luhn format Card
    val (cardNumber, cardExpiry, cardCvv, expMonth, expYear, expYearFull) = generateValidCard()

    return Persona(
      firstName = firstName,
      lastName = lastName,
      country = profile.name,
      countryCode = countryCode,
      city = city,
      streetAddress = street,
      streetAddress2 = "Apt ${Random.nextInt(1, 40)}${listOf("A", "B", "C", "").random()}",
      state = when (countryCode) {
        "US" -> listOf("CA", "NY", "TX", "FL", "IL", "PA", "OH").random()
        "GB" -> listOf("Greater London", "West Midlands", "Greater Manchester", "West Yorkshire").random()
        "EG" -> listOf("Cairo", "Giza", "Alexandria", "Dakahlia").random()
        else -> city
      },
      zipCode = zipCode,
      timezone = profile.timezone,
      language = profile.language,
      email = email,
      phoneNumber = phone,
      phoneAreaCode = areaCode,
      phoneLocalNumber = localPhone,
      birthDate = birthDate,
      birthDay = birthDay.toString(),
      birthDayPadded = String.format(Locale.US, "%02d", birthDay),
      birthMonth = String.format(Locale.US, "%02d", birthMonth),
      birthMonthNum = birthMonth.toString(),
      birthMonthName = birthMonthName,
      birthMonthShort = birthMonthShort,
      birthYear = birthYear.toString(),
      birthYearShort = (birthYear % 100).toString(),
      age = age.toString(),
      gender = if (isMale) "Male" else "Female",
      genderArabic = if (isMale) "ذكر" else "أنثى",
      title = if (isMale) "Mr" else listOf("Ms", "Mrs").random(),
      occupation = listOf("Product Reviewer", "QA Analyst", "Marketing Specialist", "Customer Success Lead", "Independent Tester").random(),
      incomeRange = listOf("$50,000 - $75,000", "$60,000 - $80,000", "$75,000 - $100,000").random(),
      educationLevel = "Bachelor's Degree",
      password = "Reviewer${Random.nextInt(2025, 2030)}#Secure",
      cardNumber = cardNumber,
      cardExpiry = cardExpiry,
      cardExpMonth = expMonth,
      cardExpYear = expYear,
      cardExpYearFull = expYearFull,
      cardCvv = cardCvv,
      cardType = if (cardNumber.startsWith("4")) "Visa" else "Mastercard"
    )
  }

  fun rotateCvv(): String {
    return String.format(Locale.US, "%03d", Random.nextInt(100, 999))
  }

  private fun generateValidCard(): CardData {
    // Generate valid Visa (4532...) or Mastercard (5424...)
    val isVisa = Random.nextBoolean()
    val prefix = if (isVisa) "4532" else "5424"
    val middle = String.format(Locale.US, "%04d%04d", Random.nextInt(1000, 9999), Random.nextInt(1000, 9999))
    val first15 = "$prefix$middle${Random.nextInt(100, 999)}"
    val checkDigit = calculateLuhnCheckDigit(first15)
    val full16 = "$first15$checkDigit"

    // Format with spaces
    val formattedCard = full16.chunked(4).joinToString(" ")
    val expMonth = String.format(Locale.US, "%02d", Random.nextInt(1, 13))
    val fullYearInt = (2027..2031).random()
    val expYear = "${fullYearInt % 100}"
    val expiry = "$expMonth/$expYear"
    val cvv = rotateCvv()

    return CardData(formattedCard, expiry, cvv, expMonth, expYear, fullYearInt.toString())
  }

  data class CardData(
    val formattedCard: String,
    val expiry: String,
    val cvv: String,
    val expMonth: String,
    val expYear: String,
    val expYearFull: String
  )

  private fun calculateLuhnCheckDigit(number: String): Int {
    var sum = 0
    val reversed = number.reversed()
    for (i in reversed.indices) {
      var digit = reversed[i].digitToInt()
      if (i % 2 == 0) { // Since 0-indexed on 15 digits, double the odd positions from right
        digit *= 2
        if (digit > 9) digit -= 9
      }
      sum += digit
    }
    val remainder = sum % 10
    return if (remainder == 0) 0 else 10 - remainder
  }

  fun getNextActiveEmail(): String {
    val available = emailPool.filterNot { usedEmails.contains(it) }
    return if (available.isNotEmpty()) {
      available.first()
    } else {
      "user_${System.currentTimeMillis() % 10000}@mailpro.net"
    }
  }

  fun markEmailUsed(email: String) {
    usedEmails.add(email)
    emailPool.remove(email)
  }

  fun addEmailsToPool(emails: List<String>) {
    emails.forEach { email ->
      val trimmed = email.trim()
      if (trimmed.isNotEmpty() && !emailPool.contains(trimmed)) {
        emailPool.add(trimmed)
      }
    }
  }

  fun getEmailPoolStats(): Pair<Int, Int> {
    return Pair(emailPool.size, usedEmails.size)
  }

  fun getLoadedEmails(): List<String> = emailPool.toList()
  fun getUsedEmails(): List<String> = usedEmails.toList()

  fun resetUsedEmails() {
    usedEmails.clear()
  }
}
