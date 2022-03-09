package nl.dannyjelsma.soundboardbot.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class UnauthorizedException : RuntimeException()