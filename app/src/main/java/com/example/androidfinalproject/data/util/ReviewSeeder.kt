package com.example.androidfinalproject.data.util

import com.example.androidfinalproject.data.model.Review
import com.google.firebase.firestore.FirebaseFirestore

object ReviewSeeder {

    private val sampleReviews = listOf(
        Review(
            movieTitle = "The Shawshank Redemption",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/kXfqcdQKsToO0OUXHcrrNCHDBzO.jpg",
            rating = 5f,
            reviewText = "A timeless masterpiece about hope and perseverance. The performances by Tim Robbins and Morgan Freeman are absolutely incredible.",
            userId = "user1",
            userFullName = "John Smith",
            timestamp = System.currentTimeMillis() - 86400000 * 7
        ),
        Review(
            movieTitle = "The Dark Knight",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/nMKdUUepR0i5zn0y1T4CsSB5chy.jpg",
            rating = 4.5f,
            reviewText = "Heath Ledger's Joker is legendary. Christopher Nolan crafted the perfect superhero film that transcends the genre.",
            userId = "user2",
            userFullName = "Emily Johnson",
            timestamp = System.currentTimeMillis() - 86400000 * 5
        ),
        Review(
            movieTitle = "Inception",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/8ZTVqvKDQ8emSGUEMjsS4yHAwrp.jpg",
            rating = 4.5f,
            reviewText = "Mind-bending and visually stunning. Nolan outdid himself with this complex yet accessible thriller.",
            userId = "user3",
            userFullName = "Michael Brown",
            timestamp = System.currentTimeMillis() - 86400000 * 3
        ),
        Review(
            movieTitle = "Pulp Fiction",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/suaEOtk1N1sgg2MTM7oZd2cfVp3.jpg",
            rating = 4f,
            reviewText = "Tarantino's dialogue is unmatched. The non-linear storytelling keeps you engaged throughout.",
            userId = "user4",
            userFullName = "Sarah Davis",
            timestamp = System.currentTimeMillis() - 86400000 * 2
        ),
        Review(
            movieTitle = "Interstellar",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
            rating = 5f,
            reviewText = "An emotional journey through space and time. The docking scene still gives me chills. Hans Zimmer's score is phenomenal.",
            userId = "user5",
            userFullName = "David Wilson",
            timestamp = System.currentTimeMillis() - 86400000
        ),
        Review(
            movieTitle = "The Matrix",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/fNG7i7RqMErkcqhohV2a6cV1Ehy.jpg",
            rating = 4.5f,
            reviewText = "Revolutionary in every sense. The action sequences and philosophical themes still hold up after all these years.",
            userId = "user6",
            userFullName = "Jessica Martinez",
            timestamp = System.currentTimeMillis() - 3600000 * 12
        ),
        Review(
            movieTitle = "Forrest Gump",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/7c9UVPPiTPltouxRVY6N9uugaVA.jpg",
            rating = 4f,
            reviewText = "Tom Hanks delivers one of his best performances. A heartwarming story that spans decades of American history.",
            userId = "user7",
            userFullName = "Robert Taylor",
            timestamp = System.currentTimeMillis() - 3600000 * 6
        ),
        Review(
            movieTitle = "Gladiator",
            movieBannerUrl = "https://image.tmdb.org/t/p/w780/hND7xAwpEhEXnWjFHGS1VY80Rh8.jpg",
            rating = 4.5f,
            reviewText = "Russell Crowe is magnificent as Maximus. The battle scenes are epic and the story is deeply moving.",
            userId = "user8",
            userFullName = "Amanda White",
            timestamp = System.currentTimeMillis() - 3600000 * 3
        )
    )

    fun seedReviews(onComplete: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val batch = firestore.batch()
        val reviewsCollection = firestore.collection("reviews")

        sampleReviews.forEach { review ->
            val docRef = reviewsCollection.document()
            batch.set(docRef, review.copy(id = docRef.id))
        }

        batch.commit()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
