
(ns proxy.protocol.v1.proxy-information-deserialiser
  (import [com.mattunderscore.tcproxy.io.serialisation AbstractByteBufferDeserialiser NotDeserialisableResult NeedsMoreDataResult DeserialisationResult]
          [java.net.Inet4Address]
          [com.mattunderscore.proxy.protocol InternetAddressFamily ProxyInformation])
  (:gen-class
    :name com.mattunderscore.proxy.protocol.v1.ProxyInformationDeserialiser
    :extends com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser))

(defmacro if-has-byte [desired-byte seq & body]
  (list 'if-let (vector 'byte (list 'first seq))
        (list 'if (list '= (list 'char 'byte) desired-byte)
              body
              (list 'fn (vector) (list 'NotDeserialisableResult/create 1)))
        (list 'fn (vector) (list 'NeedsMoreDataResult/INSTANCE))))

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
  (if-has-byte \P seq (if-has-byte \R (rest seq) (fn [] (NeedsMoreDataResult/INSTANCE)))))

(defn- read-info-from-sequence [seq]
  (process-next-byte seq {:processed 0 :header false :pending []}))

(defn -doRead
  [this buffer]

  (let [byte-seq (repeatedly #(if (.hasRemaining buffer) (.get buffer) nil))]
    (read-info-from-sequence byte-seq)))

