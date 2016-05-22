
(ns com.mattunderscore.proxy.protocol.v1.Plaintextipv4deserialiser
    (import [com.mattunderscore.tcproxy.io.serialisation AbstractByteBufferDeserialiser NotDeserialisableResult])
    (:gen-class
         :extends com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser))

(defn -doRead
  [this buffer]
  (NotDeserialisableResult/create 0))
