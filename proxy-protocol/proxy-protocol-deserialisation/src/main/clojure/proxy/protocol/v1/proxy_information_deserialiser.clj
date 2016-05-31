
(ns proxy.protocol.v1.proxy-information-deserialiser
  (import [com.mattunderscore.tcproxy.io.serialisation AbstractByteBufferDeserialiser NotDeserialisableResult NeedsMoreDataResult DeserialisationResult]
          [java.net.Inet4Address]
          [com.mattunderscore.proxy.protocol InternetAddressFamily ProxyInformation])
  (:gen-class
    :name com.mattunderscore.proxy.protocol.v1.ProxyInformationDeserialiser
    :extends com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser))

(defn- if-has-bytes [seq & forms]
  (if-let [next-byte (first seq)]
    (if (= 1 (count forms))
      (first forms)
      (let [res (apply if-has-bytes (rest seq) (rest forms))]
        (if (= (char next-byte) (first forms))
          res
          (NotDeserialisableResult/create 1))))
    (NeedsMoreDataResult/INSTANCE)))

(defn- has-info [context]
  (and (:header context)
       (:address-type context)
       (or
         (= (:address-type :unknown))
         (and (:source-address context)
              (:target-address context)
              (:source-port context)
              (:target-port context)))))

(defn- process-next-byte [seq context]
  (if-has-bytes seq \P \R \O \X \Y (NeedsMoreDataResult/INSTANCE)))

(defn- read-info-from-sequence [seq]
  (process-next-byte seq {:processed 0 :header false :pending []}))

(defn -doRead
  [this buffer]

  (let [byte-seq (repeatedly #(if (.hasRemaining buffer) (.get buffer) nil))]
    (read-info-from-sequence byte-seq)))

