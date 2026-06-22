package com.cinepass.ui.auth

/**
 * Indian states and their major cities for registration dropdown.
 */
object IndianLocationData {

    val stateCityMap: LinkedHashMap<String, List<String>> = linkedMapOf(
        "Andhra Pradesh" to listOf("Visakhapatnam", "Vijayawada", "Guntur", "Nellore", "Kurnool", "Rajahmundry", "Tirupati", "Kakinada", "Kadapa", "Anantapur", "Eluru", "Ongole", "Vizianagaram", "Machilipatnam", "Tenali", "Proddatur", "Chittoor", "Hindupur", "Bhimavaram", "Madanapalle", "Guntakal", "Srikakulam", "Dharmavaram", "Gudivada", "Narasaraopet", "Tadipatri"),
        "Arunachal Pradesh" to listOf("Itanagar", "Naharlagun", "Pasighat", "Tawang", "Ziro", "Bomdila", "Along", "Tezu", "Roing", "Changlang"),
        "Assam" to listOf("Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Nagaon", "Tinsukia", "Tezpur", "Bongaigaon", "Karimganj", "North Lakhimpur", "Sivasagar", "Goalpara", "Barpeta", "Diphu", "Dhubri"),
        "Bihar" to listOf("Patna", "Gaya", "Bhagalpur", "Muzaffarpur", "Purnia", "Darbhanga", "Bihar Sharif", "Arrah", "Begusarai", "Katihar", "Munger", "Chhapra", "Sasaram", "Hajipur", "Dehri", "Siwan", "Motihari", "Saharsa", "Bettiah", "Samastipur"),
        "Chhattisgarh" to listOf("Raipur", "Bhilai", "Bilaspur", "Korba", "Durg", "Rajnandgaon", "Raigarh", "Jagdalpur", "Ambikapur", "Dhamtari", "Mahasamund", "Chirmiri", "Kawardha"),
        "Goa" to listOf("Panaji", "Margao", "Vasco da Gama", "Mapusa", "Ponda", "Bicholim", "Curchorem", "Canacona", "Sanguem", "Quepem"),
        "Gujarat" to listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Jamnagar", "Junagadh", "Gandhinagar", "Gandhidham", "Anand", "Navsari", "Morbi", "Mehsana", "Nadiad", "Surendranagar", "Bharuch", "Porbandar", "Godhra", "Valsad", "Vapi", "Veraval", "Patan", "Botad", "Dahod"),
        "Haryana" to listOf("Gurugram", "Faridabad", "Panipat", "Ambala", "Yamunanagar", "Rohtak", "Hisar", "Karnal", "Sonipat", "Panchkula", "Bhiwani", "Sirsa", "Bahadurgarh", "Jind", "Thanesar", "Kaithal", "Rewari", "Palwal"),
        "Himachal Pradesh" to listOf("Shimla", "Dharamshala", "Solan", "Mandi", "Palampur", "Baddi", "Nahan", "Kullu", "Manali", "Hamirpur", "Una", "Bilaspur", "Chamba", "Paonta Sahib"),
        "Jharkhand" to listOf("Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Deoghar", "Hazaribagh", "Giridih", "Ramgarh", "Dumka", "Phusro", "Medininagar", "Chaibasa"),
        "Karnataka" to listOf("Bengaluru", "Mysuru", "Mangaluru", "Hubli-Dharwad", "Belagavi", "Kalaburagi", "Davanagere", "Ballari", "Vijayapura", "Shivamogga", "Tumakuru", "Raichur", "Bidar", "Hospet", "Hassan", "Gadag-Betageri", "Udupi", "Robertson Pet", "Bhadravati", "Chitradurga"),
        "Kerala" to listOf("Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam", "Palakkad", "Alappuzha", "Kannur", "Manjeri", "Kottayam", "Thalassery", "Kasaragod", "Kayamkulam", "Nedumangad", "Thodupuzha", "Vatakara", "Kanhangad", "Ponnani"),
        "Madhya Pradesh" to listOf("Bhopal", "Indore", "Jabalpur", "Gwalior", "Ujjain", "Sagar", "Dewas", "Satna", "Ratlam", "Rewa", "Murwara", "Singrauli", "Burhanpur", "Khandwa", "Bhind", "Chhindwara", "Guna", "Shivpuri", "Vidisha", "Damoh", "Mandsaur", "Khargone"),
        "Maharashtra" to listOf("Mumbai", "Pune", "Nagpur", "Thane", "Nashik", "Aurangabad", "Solapur", "Kolhapur", "Amravati", "Nanded", "Sangli", "Jalgaon", "Akola", "Latur", "Dhule", "Ahmednagar", "Chandrapur", "Parbhani", "Ichalkaranji", "Jalna", "Bhusawal", "Navi Mumbai", "Panvel", "Satara", "Beed", "Yavatmal", "Kamptee", "Osmanabad", "Wardha", "Ratnagiri"),
        "Manipur" to listOf("Imphal", "Thoubal", "Bishnupur", "Churachandpur", "Ukhrul", "Kakching", "Senapati", "Tamenglong"),
        "Meghalaya" to listOf("Shillong", "Tura", "Nongstoin", "Jowai", "Baghmara", "Williamnagar", "Resubelpara", "Nongpoh"),
        "Mizoram" to listOf("Aizawl", "Lunglei", "Champhai", "Serchhip", "Kolasib", "Saiha", "Lawngtlai", "Mamit"),
        "Nagaland" to listOf("Kohima", "Dimapur", "Mokokchung", "Tuensang", "Wokha", "Zunheboto", "Mon", "Phek"),
        "Odisha" to listOf("Bhubaneswar", "Cuttack", "Rourkela", "Berhampur", "Sambalpur", "Puri", "Balasore", "Bhadrak", "Baripada", "Jharsuguda", "Bargarh", "Paradip", "Bhawanipatna", "Dhenkanal", "Kendujhar"),
        "Punjab" to listOf("Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Mohali", "Hoshiarpur", "Batala", "Pathankot", "Moga", "Abohar", "Malerkotla", "Khanna", "Muktsar", "Barnala", "Rajpura", "Firozpur", "Kapurthala", "Faridkot", "Sangrur"),
        "Rajasthan" to listOf("Jaipur", "Jodhpur", "Kota", "Bikaner", "Ajmer", "Udaipur", "Bhilwara", "Alwar", "Bharatpur", "Sikar", "Pali", "Sri Ganganagar", "Beawar", "Jhunjhunu", "Churu", "Kishangarh", "Nagaur", "Tonk", "Hanumangarh", "Sawai Madhopur", "Baran", "Bundi", "Chittorgarh", "Dholpur", "Dungarpur", "Jaisalmer", "Jhalawar", "Karauli", "Mount Abu", "Rajsamand", "Pratapgarh"),
        "Sikkim" to listOf("Gangtok", "Namchi", "Gyalshing", "Mangan", "Rangpo", "Singtam", "Jorethang"),
        "Tamil Nadu" to listOf("Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem", "Tirunelveli", "Tiruppur", "Erode", "Vellore", "Thoothukudi", "Dindigul", "Thanjavur", "Ranipet", "Sivakasi", "Karur", "Udhagamandalam", "Hosur", "Nagercoil", "Kanchipuram", "Kumarapalayam", "Kumbakonam", "Rajapalayam", "Pudukkottai", "Ambur", "Nagapattinam"),
        "Telangana" to listOf("Hyderabad", "Warangal", "Nizamabad", "Karimnagar", "Ramagundam", "Khammam", "Mahbubnagar", "Nalgonda", "Adilabad", "Suryapet", "Siddipet", "Miryalaguda", "Jagtial", "Mancherial", "Nirmal", "Kamareddy", "Kothagudem"),
        "Tripura" to listOf("Agartala", "Dharmanagar", "Udaipur", "Kailashahar", "Belonia", "Ambassa", "Khowai", "Sabroom"),
        "Uttar Pradesh" to listOf("Lucknow", "Kanpur", "Agra", "Varanasi", "Meerut", "Prayagraj", "Ghaziabad", "Noida", "Bareilly", "Aligarh", "Moradabad", "Gorakhpur", "Saharanpur", "Jhansi", "Firozabad", "Mathura", "Muzaffarnagar", "Shahjahanpur", "Rampur", "Ayodhya", "Etawah", "Hapur", "Mirzapur", "Bulandshahr", "Sambhal", "Amroha", "Hardoi", "Fatehpur", "Raebareli", "Orai", "Unnao", "Lakhimpur Kheri", "Bahraich", "Sitapur"),
        "Uttarakhand" to listOf("Dehradun", "Haridwar", "Rishikesh", "Haldwani", "Roorkee", "Kashipur", "Rudrapur", "Nainital", "Mussoorie", "Pithoragarh", "Almora", "Srinagar", "Kotdwara", "Tehri"),
        "West Bengal" to listOf("Kolkata", "Howrah", "Asansol", "Siliguri", "Durgapur", "Bardhaman", "Malda", "Baharampur", "Habra", "Kharagpur", "Shantipur", "Dankuni", "Haldia", "Krishnanagar", "Balurghat", "Basirhat", "Bankura", "Cooch Behar", "Jalpaiguri", "Raiganj", "Purulia", "Medinipur", "Tamluk"),
        // Union Territories
        "Andaman and Nicobar Islands" to listOf("Port Blair", "Diglipur", "Rangat", "Mayabunder", "Hut Bay"),
        "Chandigarh" to listOf("Chandigarh"),
        "Dadra and Nagar Haveli and Daman and Diu" to listOf("Daman", "Diu", "Silvassa"),
        "Delhi" to listOf("New Delhi", "Delhi", "Dwarka", "Rohini", "Saket", "Karol Bagh", "Lajpat Nagar", "Connaught Place", "Pitampura", "Janakpuri", "Vasant Kunj", "Nehru Place"),
        "Jammu and Kashmir" to listOf("Srinagar", "Jammu", "Anantnag", "Baramulla", "Sopore", "Kathua", "Udhampur", "Poonch", "Rajouri", "Kupwara"),
        "Ladakh" to listOf("Leh", "Kargil", "Diskit", "Padum"),
        "Lakshadweep" to listOf("Kavaratti", "Agatti", "Minicoy", "Amini", "Andrott"),
        "Puducherry" to listOf("Puducherry", "Karaikal", "Mahe", "Yanam")
    )

    val states: List<String> = stateCityMap.keys.toList()

    fun getCitiesForState(state: String): List<String> {
        return stateCityMap[state] ?: emptyList()
    }
}
