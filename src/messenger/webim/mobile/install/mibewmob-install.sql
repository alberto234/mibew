SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

-- --------------------------------------------------------

--
-- Table structure for table `chatdevices`
--

CREATE TABLE `chatdevices` (
  `deviceid` int(11) NOT NULL AUTO_INCREMENT,
  `clientdeviceid` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `devicename` varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`deviceid`),
  UNIQUE KEY `CLIENTDEVICEIDX` (`clientdeviceid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `chatmessagesfromdevice`
--

CREATE TABLE `chatmessagesfromdevice` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deviceid` int(11) NOT NULL,
  `messageid` int(11) NOT NULL,
  `devicemessageid` int(11) NOT NULL,
  `msgtimestamp` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `chatoperatorsession`
--

CREATE TABLE `chatoperatorsession` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `oprtoken` varchar(20) NOT NULL,
  `dtmexpires` datetime DEFAULT NULL,
  `operatorid` int(11) NOT NULL,
  `deviceid` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `chatsyncedmessages`
--

CREATE TABLE `chatsyncedmessages` (
  `syncmessageid` int(11) NOT NULL AUTO_INCREMENT,
  `messageid` int(11) NOT NULL,
  `deviceid` int(11) NOT NULL,
  PRIMARY KEY (`syncmessageid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `chatsyncedthreads`
--

CREATE TABLE `chatsyncedthreads` (
  `syncthreadid` int(11) NOT NULL AUTO_INCREMENT,
  `threadid` int(11) NOT NULL,
  `deviceid` int(11) NOT NULL,
  `state` int(11) NOT NULL,
  `shownmessageid` int(11) DEFAULT NULL,
  PRIMARY KEY (`syncthreadid`),
  UNIQUE KEY `device_thread` (`threadid`,`deviceid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;
